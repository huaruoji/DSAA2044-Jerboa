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
from recommendation_inference import RecommendationModel
import os

# Initialize Flask app
app = Flask(__name__)

# Load the recommendation model
model = RecommendationModel(models_dir='models')

# Load model at startup
print("Loading recommendation model...")
model.load_models()
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


@app.route('/api/recommend', methods=['POST'])
def recommend():
    """
    Get recommendations based on a query.
    
    Request Body:
    {
        "query": "machine learning datasets",
        "top_k": 5,              // optional, default: 5
        "min_score": 0.0         // optional, default: 0.0
    }
    
    Response:
    {
        "success": true,
        "query": "machine learning datasets",
        "count": 5,
        "recommendations": [
            {
                "title": "...",
                "text": "...",
                "url": "...",
                "subreddit": "...",
                "score": 123,
                "similarity_score": 0.95,
                "created_utc": 1646160815
            },
            ...
        ]
    }
    """
    try:
        # Parse request data
        data = request.get_json()
        
        if not data or 'query' not in data:
            return jsonify({
                'success': False,
                'error': 'Missing required field: query'
            }), 400
        
        query = data['query']
        top_k = data.get('top_k', 5)
        min_score = data.get('min_score', 0.0)
        
        # Validate parameters
        if not isinstance(query, str) or not query.strip():
            return jsonify({
                'success': False,
                'error': 'Query must be a non-empty string'
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
