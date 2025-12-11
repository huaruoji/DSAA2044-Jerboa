"""
TF-IDF Based Recommender
========================
Content-based recommendation using TF-IDF vectorization and cosine similarity.

Author: DSAA2044 Team
Date: December 2025
"""

import pickle
import re
import pandas as pd
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from typing import List, Dict, Any
from base_recommender import BaseRecommender


class TFIDFRecommender(BaseRecommender):
    """
    TF-IDF based content recommendation system.
    Supports history-based recommendations.
    """
    
    def __init__(self, models_dir: str = 'models'):
        super().__init__(models_dir)
        self.vectorizer = None
        self.tfidf_matrix = None
        self.df = None
        self.metadata = None
    
    def load_model(self):
        """Load TF-IDF model artifacts from disk."""
        print("Loading TF-IDF recommendation model...")
        
        # Load vectorizer
        with open(f'{self.models_dir}/tfidf_vectorizer.pkl', 'rb') as f:
            self.vectorizer = pickle.load(f)
        
        # Load TF-IDF matrix
        with open(f'{self.models_dir}/tfidf_matrix.pkl', 'rb') as f:
            self.tfidf_matrix = pickle.load(f)
        
        # Load processed dataframe
        with open(f'{self.models_dir}/processed_posts.pkl', 'rb') as f:
            self.df = pickle.load(f)
        
        # Load metadata
        with open(f'{self.models_dir}/model_metadata.pkl', 'rb') as f:
            self.metadata = pickle.load(f)
        
        self.is_loaded = True
        print(f"✓ Model loaded: {self.metadata['num_posts']:,} posts, "
              f"{self.metadata['num_features']:,} features")
    
    @staticmethod
    def clean_text(text: str) -> str:
        """Clean and normalize text."""
        if not isinstance(text, str):
            return ""
        
        text = text.lower()
        text = re.sub(r'http\S+|www\S+|https\S+', '', text, flags=re.MULTILINE)
        text = re.sub(r'[^a-zA-Z0-9\s]', ' ', text)
        text = re.sub(r'\s+', ' ', text).strip()
        
        return text
    
    def score_candidates(
        self,
        history_contents: List[str],
        candidates: List[Dict[str, Any]]
    ) -> List[Dict[str, Any]]:
        """
        Score candidate posts based on user's reading history.
        
        Args:
            history_contents: User's reading history
            candidates: List of dicts with 'id', 'title', 'body'
            
        Returns:
            List of {'id': str, 'similarity_score': float}
        """
        if not self.is_loaded:
            raise RuntimeError("Model not loaded. Call load_model() first.")
        
        if not history_contents or not candidates:
            return [{'id': c.get('id', ''), 'similarity_score': 0.0} for c in candidates]
        
        # Build user profile from history
        combined_history = ' '.join(history_contents)
        cleaned_history = self.clean_text(combined_history)
        
        if not cleaned_history:
            return [{'id': c.get('id', ''), 'similarity_score': 0.0} for c in candidates]
        
        # Vectorize user profile
        history_vector = self.vectorizer.transform([cleaned_history])
        
        # Process and vectorize each candidate
        results = []
        for candidate in candidates:
            candidate_id = candidate.get('id', '')
            title = candidate.get('title', '')
            body = candidate.get('body', '')
            
            # Combine title and body (title weighted 2x)
            combined_text = f"{title} {title} {body}"
            cleaned_text = self.clean_text(combined_text)
            
            if not cleaned_text:
                results.append({'id': candidate_id, 'similarity_score': 0.0})
                continue
            
            # Vectorize candidate
            candidate_vector = self.vectorizer.transform([cleaned_text])
            
            # Calculate similarity
            similarity = cosine_similarity(history_vector, candidate_vector)[0][0]
            
            results.append({
                'id': candidate_id,
                'similarity_score': float(similarity)
            })
        
        return results
    
    def recommend_from_history(
        self,
        history_contents: List[str],
        top_k: int = 10,
        min_score: float = 0.0,
        exclude_ids: List[str] = None
    ) -> List[Dict[str, Any]]:
        """
        [LEGACY] Recommend posts from training dataset based on history.
        
        Algorithm:
        1. Combine all history texts into a single "user profile"
        2. Vectorize the user profile
        3. Calculate cosine similarity with all posts
        4. Return top-K most similar posts (excluding already seen)
        """
        if not self.is_loaded:
            raise RuntimeError("Model not loaded. Call load_model() first.")
        
        if not history_contents:
            return []
        
        # Combine all history texts into one document
        combined_history = ' '.join(history_contents)
        cleaned_history = self.clean_text(combined_history)
        
        if not cleaned_history:
            return []
        
        # Vectorize user's history
        history_vector = self.vectorizer.transform([cleaned_history])
        
        # Calculate similarity with all posts
        similarity_scores = cosine_similarity(history_vector, self.tfidf_matrix).flatten()
        
        # Filter by minimum score
        valid_mask = similarity_scores >= min_score
        
        # Exclude already seen posts if IDs provided
        if exclude_ids:
            exclude_set = set(exclude_ids)
            if 'id' in self.df.columns:
                exclude_mask = ~self.df['id'].isin(exclude_set)
                valid_mask = valid_mask & exclude_mask
        
        valid_indices = np.where(valid_mask)[0]
        
        if len(valid_indices) == 0:
            return []
        
        # Get top-K
        valid_scores = similarity_scores[valid_indices]
        top_k = min(top_k, len(valid_indices))
        top_local_indices = valid_scores.argsort()[-top_k:][::-1]
        top_indices = valid_indices[top_local_indices]
        
        # Build results
        results = []
        for idx in top_indices:
            post = self.df.iloc[idx]
            
            # Safe type conversions
            try:
                created_utc = int(float(post['created_utc']))
            except:
                try:
                    created_utc = int(pd.to_datetime(post['created_utc']).timestamp())
                except:
                    created_utc = 0
            
            try:
                score = int(float(post['score']))
            except:
                score = 0
            
            post_id = str(post['id']) if 'id' in post.index else f"post_{idx}"
            
            results.append({
                'id': post_id,
                'title': str(post['title']),
                'text': str(post['combined_text'])[:500],  # Truncate for API
                'url': str(post.get('url', '')),
                'subreddit': str(post['subreddit.name']),
                'score': score,
                'similarity_score': float(similarity_scores[idx]),
                'created_utc': created_utc
            })
        
        return results
    
    def get_model_info(self) -> Dict[str, Any]:
        """Return model metadata."""
        if not self.is_loaded:
            raise RuntimeError("Model not loaded.")
        
        return {
            'algorithm': 'TF-IDF',
            'num_posts': self.metadata['num_posts'],
            'num_features': self.metadata['num_features'],
            'training_date': self.metadata['training_date'],
            'max_features': self.metadata.get('max_features', 10000),
            'strategy': self.metadata.get('recommendation_strategy', 'content-based')
        }


# Backward compatibility: keep old class name
RecommendationModel = TFIDFRecommender
