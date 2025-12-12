"""
Flask API for Content-Based Recommendation System
==================================================
A simple Flask REST API for the recommendation system.

Endpoints:
- GET  /api/health          - Health check
- GET  /api/model/info      - Get model information
- POST /api/recommend       - Get recommendations for a query

Author: DSAA2044 Student
Date: December 2025
"""

from flask import Flask, request, jsonify
from base_recommender import RecommenderFactory
import os

# Initialize Flask app
app = Flask(__name__)

# Create recommender using factory pattern
# Easy to switch: change 'tfidf' to 'bert' when ready
ALGORITHM = os.getenv('RECOMMENDER_ALGORITHM', 'tfidf')
model = RecommenderFactory.create_recommender(algorithm=ALGORITHM, models_dir='models')

# Load model at startup
print(f"Loading {ALGORITHM.upper()} recommendation model...")
model.load_model()
print("✓ Model loaded successfully!")


# ============================================================================
# API ENDPOINTS
# ============================================================================

@app.route('/api/health', methods=['GET'])
def health_check():
    """Health check endpoint."""
    return jsonify({
        'status': 'healthy',
        'service': 'Content-Based Recommendation API',
        'version': '1.0.0'
    })


@app.route('/api/model/info', methods=['GET'])
def get_model_info():
    """Get information about the loaded model."""
    try:
        info = model.get_model_info()
        return jsonify({
            'success': True,
            'data': info
        })
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


@app.route('/api/score', methods=['POST'])
def score_candidates():
    """
    Score candidate posts based on user's reading history.
    Used for Lemmy posts from Global/Local feeds.
    
    Request Body:
    {
        "history_contents": ["user history 1", "user history 2", ...],
        "candidates": [
            {"id": "123", "title": "...", "body": "..."},
            {"id": "456", "title": "...", "body": "..."}
        ],
        "top_k": 10  // optional, default: return all scored
    }
    
    Response:
    {
        "success": true,
        "algorithm": "tfidf",
        "scored_candidates": [
            {"id": "456", "similarity_score": 0.85},
            {"id": "123", "similarity_score": 0.62}
        ]
    }
    """
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({
                'success': False,
                'error': 'No JSON data provided'
            }), 400
        
        history_contents = data.get('history_contents', [])
        candidates = data.get('candidates', [])
        top_k = data.get('top_k', None)
        
        if not candidates:
            return jsonify({
                'success': False,
                'error': 'No candidates provided'
            }), 400
        
        # If no history, return candidates with equal scores
        if not history_contents:
            scored = [
                {'id': c.get('id', ''), 'similarity_score': 0.0}
                for c in candidates
            ]
            return jsonify({
                'success': True,
                'algorithm': ALGORITHM,
                'scored_candidates': scored,
                'note': 'No history available, returning unscored candidates'
            })
        
        # Score candidates using the model
        scored_candidates = model.score_candidates(
            history_contents=history_contents,
            candidates=candidates
        )
        
        # Sort by similarity score descending
        scored_candidates.sort(key=lambda x: x['similarity_score'], reverse=True)
        
        # Apply top_k if specified
        if top_k and top_k > 0:
            scored_candidates = scored_candidates[:top_k]
        
        return jsonify({
            'success': True,
            'algorithm': ALGORITHM,
            'scored_candidates': scored_candidates,
            'count': len(scored_candidates)
        })
        
    except Exception as e:
        import traceback
        print(f"ERROR in /api/score: {e}")
        print(traceback.format_exc())
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


@app.route('/api/recommend', methods=['GET', 'POST'])
def recommend():
    """
    [LEGACY] Get recommendations from training dataset.
    
    Request Body (NEW FORMAT):
    {
        "history_contents": ["post title 1", "post text 2", ...],
        "top_k": 10,              // optional, default: 10
        "min_score": 0.0,         // optional, default: 0.0
        "exclude_ids": []         // optional, IDs to exclude
    }
    
    Legacy support (DEPRECATED):
    {
        "query": "single keyword search",
        "top_k": 5
    }
    
    Response:
    {
        "success": true,
        "count": 10,
        "algorithm": "TF-IDF",
        "recommendations": [...]
    }
    """
    try:
        # Support both GET (legacy) and POST (new) requests
        if request.method == 'GET':
            # Legacy GET support with query parameters
            query = request.args.get('q') or request.args.get('query')
            if not query:
                return jsonify({
                    'success': False,
                    'error': 'Missing required parameter: q or query'
                }), 400
            
            history_contents = [query]
            top_k = int(request.args.get('top_k', 10))
            min_score = float(request.args.get('min_score', 0.0))
            exclude_ids = []
            
        else:
            # POST request with JSON body
            data = request.get_json()
            
            if not data:
                return jsonify({
                    'success': False,
                    'error': 'Request body is required'
                }), 400
            
            # Support both new and legacy POST formats
            history_contents = data.get('history_contents')
            
            # Legacy support: convert query to history_contents
            if not history_contents and 'query' in data:
                history_contents = [data['query']]
            
            if not history_contents:
                return jsonify({
                    'success': False,
                    'error': 'Missing required field: history_contents'
                }), 400
            
            # Get parameters
            top_k = data.get('top_k', 10)
            min_score = data.get('min_score', 0.0)
            exclude_ids = data.get('exclude_ids', [])
        
        # Validate parameters
        if not isinstance(history_contents, list):
            return jsonify({
                'success': False,
                'error': 'history_contents must be a list of strings'
            }), 400
        
        if not isinstance(top_k, int) or top_k < 1 or top_k > 100:
            return jsonify({
                'success': False,
                'error': 'top_k must be an integer between 1 and 100'
            }), 400
        
        if not isinstance(min_score, (int, float)) or min_score < 0 or min_score > 1:
            return jsonify({
                'success': False,
                'error': 'min_score must be a number between 0 and 1'
            }), 400
        
        # Get recommendations using history-based algorithm
        recommendations = model.recommend_from_history(
            history_contents=history_contents,
            top_k=top_k,
            min_score=min_score,
            exclude_ids=exclude_ids
        )
        
        # Get model info
        model_info = model.get_model_info()
        
        # Return response
        return jsonify({
            'success': True,
            'algorithm': model_info.get('algorithm', 'Unknown'),
            'count': len(recommendations),
            'recommendations': recommendations
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


@app.route('/api/recommend', methods=['GET'])
def recommend_get():
    """
    Get recommendations using query parameters (alternative to POST).
    
    Query Parameters:
    - q or query: Search query (required)
    - top_k: Number of results (optional, default: 5)
    - min_score: Minimum similarity score (optional, default: 0.0)
    
    Example: /api/recommend?q=machine+learning&top_k=10
    """
    try:
        # Get query parameters
        query = request.args.get('q') or request.args.get('query')
        
        if not query:
            return jsonify({
                'success': False,
                'error': 'Missing required parameter: q or query'
            }), 400
        
        top_k = int(request.args.get('top_k', 5))
        min_score = float(request.args.get('min_score', 0.0))
        
        # Validate parameters
        if top_k < 1 or top_k > 100:
            return jsonify({
                'success': False,
                'error': 'top_k must be between 1 and 100'
            }), 400
        
        if min_score < 0 or min_score > 1:
            return jsonify({
                'success': False,
                'error': 'min_score must be between 0 and 1'
            }), 400
        
        # Get recommendations
        recommendations = model.recommend(
            query=query,
            top_k=top_k,
            min_score=min_score
        )
        
        # Return response
        return jsonify({
            'success': True,
            'query': query,
            'count': len(recommendations),
            'recommendations': recommendations
        })
        
    except ValueError as e:
        return jsonify({
            'success': False,
            'error': f'Invalid parameter value: {str(e)}'
        }), 400
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


# ============================================================================
# ERROR HANDLERS
# ============================================================================

@app.errorhandler(404)
def not_found(e):
    """Handle 404 errors."""
    return jsonify({
        'success': False,
        'error': 'Endpoint not found'
    }), 404


@app.errorhandler(405)
def method_not_allowed(e):
    """Handle 405 errors."""
    return jsonify({
        'success': False,
        'error': 'Method not allowed'
    }), 405


@app.errorhandler(500)
def internal_error(e):
    """Handle 500 errors."""
    return jsonify({
        'success': False,
        'error': 'Internal server error'
    }), 500


# ============================================================================
# MAIN
# ============================================================================

if __name__ == '__main__':
    print("=" * 80)
    print("Content-Based Recommendation API Server")
    print("=" * 80)
    print("\nEndpoints:")
    print("  GET  /api/health")
    print("  GET  /api/model/info")
    print("  POST /api/recommend")
    print("  GET  /api/recommend?q=<query>&top_k=<n>")
    print("\n" + "=" * 80)
    
    # Run the Flask app
    app.run(
        host='0.0.0.0',
        port=5000,
        debug=True
    )
