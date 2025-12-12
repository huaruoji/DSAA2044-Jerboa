"""
Base Recommender Architecture
==============================
Extensible recommendation system supporting multiple algorithms:
- TF-IDF (current baseline)
- BERT (future upgrade)
- Hybrid models

Author: DSAA2044 Team
Date: December 2025
"""

from abc import ABC, abstractmethod
from typing import List, Dict, Any
import numpy as np


class BaseRecommender(ABC):
    """
    Abstract base class for all recommendation algorithms.
    Ensures consistent interface for easy model swapping.
    """
    
    def __init__(self, models_dir: str = 'models'):
        self.models_dir = models_dir
        self.is_loaded = False
    
    @abstractmethod
    def load_model(self):
        """Load model artifacts from disk."""
        pass
    
    @abstractmethod
    def recommend_from_history(
        self, 
        history_contents: List[str], 
        top_k: int = 10,
        min_score: float = 0.0,
        exclude_ids: List[str] = None
    ) -> List[Dict[str, Any]]:
        """
        Generate recommendations based on user's reading history.
        
        Parameters:
        -----------
        history_contents : List[str]
            List of post titles/texts user has recently read
        top_k : int
            Number of recommendations to return
        min_score : float
            Minimum similarity threshold
        exclude_ids : List[str]
            IDs of posts to exclude (already seen)
            
        Returns:
        --------
        List[Dict[str, Any]]
            List of recommended posts with metadata
        """
        pass
    
    @abstractmethod
    def score_candidates(
        self,
        history_contents: List[str],
        candidates: List[Dict[str, Any]]
    ) -> List[Dict[str, Any]]:
        """
        Score candidate posts based on user's reading history.
        Used for ranking real-time Lemmy posts.
        
        Parameters:
        -----------
        history_contents : List[str]
            List of post titles/texts user has recently read
        candidates : List[Dict[str, Any]]
            List of candidate posts, each with 'id', 'title', 'body'
            
        Returns:
        --------
        List[Dict[str, Any]]
            List of {'id': str, 'similarity_score': float}
        """
        pass
    
    @abstractmethod
    def get_model_info(self) -> Dict[str, Any]:
        """Return metadata about the loaded model."""
        pass


class RecommenderFactory:
    """
    Factory pattern for creating recommender instances.
    Makes it easy to switch between different algorithms.
    """
    
    @staticmethod
    def create_recommender(algorithm: str = 'tfidf', **kwargs) -> BaseRecommender:
        """
        Create a recommender instance based on algorithm type.
        
        Parameters:
        -----------
        algorithm : str
            'tfidf' or 'bert' or 'hybrid'
        **kwargs : dict
            Additional parameters for specific algorithms
            
        Returns:
        --------
        BaseRecommender
            Initialized recommender instance
        """
        if algorithm.lower() == 'tfidf':
            from tfidf_recommender import TFIDFRecommender
            return TFIDFRecommender(**kwargs)
        elif algorithm.lower() == 'bert':
            # Future: from bert_recommender import BERTRecommender
            # return BERTRecommender(**kwargs)
            raise NotImplementedError("BERT recommender coming soon!")
        elif algorithm.lower() == 'hybrid':
            # Future: hybrid model combining TF-IDF and BERT
            raise NotImplementedError("Hybrid recommender coming soon!")
        else:
            raise ValueError(f"Unknown algorithm: {algorithm}")
