"""
Recommendation Model Inference Module
======================================
This module provides functions to load the trained recommendation model
and make predictions. Use this in your Flask/FastAPI server.

Author: DSAA2044 Student
Date: December 2025
"""

import pickle
import re
import pandas as pd
from sklearn.metrics.pairwise import cosine_similarity


class RecommendationModel:
    """
    A class to handle loading and inference for the content-based
    recommendation system.
    """
    
    def __init__(self, models_dir='models'):
        """
        Initialize the recommendation model by loading artifacts.
        
        Parameters:
        -----------
        models_dir : str
            Directory containing the saved model artifacts
        """
        self.models_dir = models_dir
        self.vectorizer = None
        self.tfidf_matrix = None
        self.df = None
        self.metadata = None
        
    def load_models(self):
        """Load all model artifacts from disk."""
        print("Loading recommendation model artifacts...")
        
        # Load vectorizer
        with open(f'{self.models_dir}/tfidf_vectorizer.pkl', 'rb') as f:
            self.vectorizer = pickle.load(f)
        print("✓ Loaded TF-IDF vectorizer")
        
        # Load TF-IDF matrix
        with open(f'{self.models_dir}/tfidf_matrix.pkl', 'rb') as f:
            self.tfidf_matrix = pickle.load(f)
        print("✓ Loaded TF-IDF matrix")
        
        # Load processed dataframe
        with open(f'{self.models_dir}/processed_posts.pkl', 'rb') as f:
            self.df = pickle.load(f)
        print("✓ Loaded processed posts dataframe")
        
        # Load metadata
        with open(f'{self.models_dir}/model_metadata.pkl', 'rb') as f:
            self.metadata = pickle.load(f)
        print("✓ Loaded model metadata")
        
        print(f"\nModel Info:")
        print(f"  - Number of posts: {self.metadata['num_posts']:,}")
        print(f"  - Vocabulary size: {self.metadata['num_features']:,}")
        print(f"  - Training date: {self.metadata['training_date']}")
        
    @staticmethod
    def clean_text(text):
        """
        Clean text by:
        - Converting to lowercase
        - Removing URLs
        - Removing special characters
        - Removing extra whitespace
        """
        if not isinstance(text, str):
            return ""
        
        # Convert to lowercase
        text = text.lower()
        
        # Remove URLs
        text = re.sub(r'http\S+|www\S+|https\S+', '', text, flags=re.MULTILINE)
        
        # Remove special characters, keeping only letters, numbers, and spaces
        text = re.sub(r'[^a-zA-Z0-9\s]', ' ', text)
        
        # Remove extra whitespace
        text = re.sub(r'\s+', ' ', text).strip()
        
        return text
    
    def recommend(self, query, top_k=5, min_score=0.0):
        """
        Recommend posts based on a user query.
        
        Parameters:
        -----------
        query : str
            User's search query or keywords
        top_k : int, default=5
            Number of top recommendations to return
        min_score : float, default=0.0
            Minimum similarity score threshold (0 to 1)
        
        Returns:
        --------
        list of dict
            List of recommended posts with metadata
        """
        if self.vectorizer is None or self.tfidf_matrix is None:
            raise RuntimeError("Model not loaded. Call load_models() first.")
        
        # Clean the query
        cleaned_query = self.clean_text(query)
        
        if not cleaned_query:
            return []
        
        # Transform query using the fitted vectorizer
        query_vector = self.vectorizer.transform([cleaned_query])
        
        # Calculate cosine similarity
        similarity_scores = cosine_similarity(query_vector, self.tfidf_matrix).flatten()
        
        # Filter by minimum score
        valid_indices = similarity_scores >= min_score
        filtered_scores = similarity_scores[valid_indices]
        filtered_indices = valid_indices.nonzero()[0]
        
        if len(filtered_indices) == 0:
            return []
        
        # Get top_k indices
        top_k = min(top_k, len(filtered_indices))
        top_local_indices = filtered_scores.argsort()[-top_k:][::-1]
        top_indices = filtered_indices[top_local_indices]
        
        # Build results
        results = []
        for idx in top_indices:
            post = self.df.iloc[idx]
            results.append({
                'title': post['title'],
                'text': post['combined_text'],
                'url': post['url'],
                'subreddit': post['subreddit.name'],
                'score': int(post['score']),
                'similarity_score': float(similarity_scores[idx]),
                'created_utc': int(post['created_utc'])
            })
        
        return results
    
    def get_model_info(self):
        """Return information about the loaded model."""
        if not self.metadata:
            raise RuntimeError("Model not loaded. Call load_models() first.")
        
        return {
            'num_posts': self.metadata['num_posts'],
            'num_features': self.metadata['num_features'],
            'training_date': self.metadata['training_date'],
            'max_features': self.metadata['max_features']
        }


# ============================================================================
# EXAMPLE USAGE
# ============================================================================

if __name__ == "__main__":
    # Initialize and load the model
    model = RecommendationModel(models_dir='models')
    model.load_models()
    
    print("\n" + "=" * 80)
    print("Testing Recommendation System")
    print("=" * 80)
    
    # Test queries
    test_queries = [
        "machine learning datasets",
        "web scraping data collection",
        "climate change temperature data",
        "stock market financial data"
    ]
    
    for query in test_queries:
        print(f"\n📌 Query: '{query}'")
        print("-" * 80)
        
        recommendations = model.recommend(query, top_k=3, min_score=0.1)
        
        if recommendations:
            for i, rec in enumerate(recommendations, 1):
                print(f"\n{i}. {rec['title']}")
                print(f"   Similarity: {rec['similarity_score']:.4f}")
                print(f"   Subreddit: r/{rec['subreddit']}")
                print(f"   Score: {rec['score']}")
                print(f"   Preview: {rec['text'][:100]}...")
        else:
            print("   No recommendations found.")
    
    print("\n" + "=" * 80)
    print("Model Info:")
    print("=" * 80)
    info = model.get_model_info()
    for key, value in info.items():
        print(f"  {key}: {value}")
