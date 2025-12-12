"""
Content-Based Recommendation System Training Script
====================================================
This script trains a baseline recommendation model for a social media app.

The model uses TF-IDF vectorization and Cosine Similarity to recommend
posts based on content similarity to a user query.

Author: DSAA2044 Student
Date: December 2025
"""

import pandas as pd
import numpy as np
import re
import pickle
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import os

# ============================================================================
# 1. DATA LOADING
# ============================================================================

print("=" * 80)
print("STEP 1: Loading Data")
print("=" * 80)

# Load the posts dataset - Auto-detect format
# Try merged file first, then fall back to individual files
data_path = os.path.join('data', 'merged_reddit_data.csv')

if not os.path.exists(data_path):
    # Try the old format
    data_path = os.path.join('data', 'the-reddit-dataset-dataset-posts.csv')

print(f"Loading data from: {data_path}")

df = pd.read_csv(data_path)
print(f"✓ Loaded {len(df)} posts")
print(f"✓ Columns: {list(df.columns)}")

# Display basic info about the dataset
print(f"\nDataset shape: {df.shape}")
print(f"Memory usage: {df.memory_usage(deep=True).sum() / 1024**2:.2f} MB")

# Detect and normalize column names based on dataset format
print("\nNormalizing column names...")
column_mapping = {}

# Map different possible column names to standard names
if 'Text' in df.columns and 'Title' not in df.columns:
    # merged_reddit_data.csv format
    column_mapping = {
        'Text': 'combined_text_raw',
        'Subreddit': 'subreddit.name',
        'Upvotes': 'score',
        'creation_date': 'created_utc',
        'ID': 'id'
    }
    df['title'] = ''  # No separate title in this format
    df['selftext'] = ''  # Text is already combined
elif 'Title' in df.columns and 'Body' in df.columns:
    # Individual subreddit files format
    column_mapping = {
        'Title': 'title',
        'Body': 'selftext',
        'Subreddit': 'subreddit.name',
        'Upvotes': 'score',
        'URL': 'url',
        'creation_date': 'created_utc',
        'ID': 'id'
    }
elif 'title' in df.columns and 'selftext' in df.columns:
    # Old format - already has correct column names
    column_mapping = {
        'subreddit.name': 'subreddit.name',
        'score': 'score',
        'url': 'url',
        'created_utc': 'created_utc'
    }

# Apply column mapping
for old_col, new_col in column_mapping.items():
    if old_col in df.columns:
        if new_col not in df.columns:
            df[new_col] = df[old_col]

print(f"✓ Column mapping applied")
print(f"✓ Normalized columns: {list(df.columns)}")

# ============================================================================
# 2. HANDLE MISSING VALUES
# ============================================================================

print("\n" + "=" * 80)
print("STEP 2: Handling Missing Values")
print("=" * 80)

# Check for missing values
print("\nMissing values before cleaning:")

# Ensure required columns exist
if 'title' not in df.columns:
    df['title'] = ''
if 'selftext' not in df.columns:
    df['selftext'] = ''

print(df[['title', 'selftext']].isnull().sum())

# Fill missing values with empty string
df['selftext'] = df['selftext'].fillna('')
df['title'] = df['title'].fillna('')

# If we have pre-combined text, use it
if 'combined_text_raw' in df.columns:
    df['title'] = df['combined_text_raw'].fillna('')
    df['selftext'] = ''

print("\nMissing values after cleaning:")
print(df[['title', 'selftext']].isnull().sum())
print("✓ Missing values handled")

# ============================================================================
# 3. TEXT PREPROCESSING
# ============================================================================

print("\n" + "=" * 80)
print("STEP 3: Text Preprocessing")
print("=" * 80)

def clean_text(text):
    """
    Clean text by:
    - Converting to lowercase
    - Removing URLs
    - Removing special characters (keeping only alphanumeric and spaces)
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

# Create combined_text column by concatenating title and body
# NOTE: We deliberately DO NOT include subreddit name in the text
# This ensures recommendations are based on content similarity, not subreddit matching
print("\nCreating 'combined_text' column...")
print("ℹ️  Strategy: Content-based only (not including subreddit name)")
df['combined_text'] = df['title'] + ' ' + df['selftext']

# Apply text cleaning
print("Applying text cleaning...")
df['combined_text'] = df['combined_text'].apply(clean_text)

# Remove posts with empty combined_text after cleaning
initial_count = len(df)
df = df[df['combined_text'].str.len() > 0].reset_index(drop=True)
final_count = len(df)

print(f"✓ Created and cleaned 'combined_text' column")
print(f"✓ Removed {initial_count - final_count} posts with empty text")
print(f"✓ Final dataset size: {final_count} posts")

# Display sample of cleaned text
print("\nSample of cleaned text:")
print("-" * 80)
for i in range(min(3, len(df))):
    print(f"\nPost {i+1}:")
    print(f"Title: {df.iloc[i]['title'][:100]}...")
    print(f"Combined: {df.iloc[i]['combined_text'][:150]}...")

# ============================================================================
# 4. TF-IDF VECTORIZATION
# ============================================================================

print("\n" + "=" * 80)
print("STEP 4: TF-IDF Vectorization")
print("=" * 80)

# Initialize TF-IDF Vectorizer with improved parameters for content-based recommendations
print("\nInitializing TfidfVectorizer with the following parameters:")
print("  - max_features: 10000 (increased for better content representation)")
print("  - stop_words: 'english'")
print("  - ngram_range: (1, 3) [unigrams, bigrams, and trigrams]")
print("  - min_df: 2 [ignore terms appearing in less than 2 documents]")
print("  - max_df: 0.7 [ignore terms appearing in more than 70% of documents]")
print("  - sublinear_tf: True [use logarithmic term frequency scaling]")
print("\nℹ️  This configuration emphasizes content similarity over subreddit matching")

vectorizer = TfidfVectorizer(
    max_features=10000,  # Increased from 5000 for richer vocabulary
    stop_words='english',
    ngram_range=(1, 3),  # Include unigrams, bigrams, and trigrams
    min_df=2,  # Minimum document frequency
    max_df=0.7,  # Stricter maximum to filter out very common terms
    sublinear_tf=True  # Use log scaling for term frequency
)

# Fit and transform the combined_text
print("\nFitting vectorizer and transforming text...")
tfidf_matrix = vectorizer.fit_transform(df['combined_text'])

print(f"✓ TF-IDF matrix shape: {tfidf_matrix.shape}")
print(f"✓ Number of documents: {tfidf_matrix.shape[0]}")
print(f"✓ Number of features (vocabulary size): {tfidf_matrix.shape[1]}")
print(f"✓ Matrix sparsity: {(1.0 - tfidf_matrix.nnz / (tfidf_matrix.shape[0] * tfidf_matrix.shape[1])) * 100:.2f}%")

# Display some feature names
print("\nSample feature names (terms in vocabulary):")
feature_names = vectorizer.get_feature_names_out()
print(feature_names[:20])

# ============================================================================
# 5. COSINE SIMILARITY EXPLANATION
# ============================================================================

print("\n" + "=" * 80)
print("STEP 5: Similarity Calculation Method")
print("=" * 80)

print("""
COSINE SIMILARITY for Content-Based Recommendations
---------------------------------------------------

What is Cosine Similarity?
- Cosine similarity measures the cosine of the angle between two vectors
- It ranges from -1 to 1, where:
  * 1 means the vectors are identical (perfectly similar)
  * 0 means the vectors are orthogonal (no similarity)
  * -1 means the vectors are opposite
  
Why use it for recommendations?
- Works well with high-dimensional sparse data (like TF-IDF vectors)
- Focuses on the angle between vectors, not their magnitude
- Effectively captures semantic similarity between texts
- Computationally efficient

How it works in our system:
1. User provides a query (e.g., "machine learning datasets")
2. Query is transformed into a TF-IDF vector using the fitted vectorizer
3. Cosine similarity is calculated between query vector and all post vectors
4. Posts are ranked by similarity score (highest = most similar)
5. Top-K most similar posts are returned as recommendations

Formula: similarity = (A · B) / (||A|| × ||B||)
where A is the query vector and B is a post vector
""")

# ============================================================================
# 6. RECOMMENDATION FUNCTION
# ============================================================================

print("\n" + "=" * 80)
print("STEP 6: Creating Recommendation Function")
print("=" * 80)

def recommend(query, top_k=5, return_scores=True):
    """
    Recommend posts based on a user query using cosine similarity.
    
    Parameters:
    -----------
    query : str
        User's search query or keywords
    top_k : int, default=5
        Number of top recommendations to return
    return_scores : bool, default=True
        Whether to return similarity scores
    
    Returns:
    --------
    pandas.DataFrame
        DataFrame containing recommended posts with titles, text, and scores
    """
    # Clean the query text
    cleaned_query = clean_text(query)
    
    if not cleaned_query:
        print("Warning: Query is empty after cleaning. Returning empty results.")
        return pd.DataFrame()
    
    # Transform query using the fitted vectorizer
    query_vector = vectorizer.transform([cleaned_query])
    
    # Calculate cosine similarity between query and all posts
    similarity_scores = cosine_similarity(query_vector, tfidf_matrix).flatten()
    
    # Get indices of top_k most similar posts
    top_indices = similarity_scores.argsort()[-top_k:][::-1]
    
    # Create results DataFrame
    results = df.iloc[top_indices].copy()
    results['similarity_score'] = similarity_scores[top_indices]
    
    # Select relevant columns to return
    if return_scores:
        output_cols = ['title', 'combined_text', 'similarity_score', 'score', 'url']
    else:
        output_cols = ['title', 'combined_text', 'score', 'url']
    
    # Filter to available columns
    output_cols = [col for col in output_cols if col in results.columns]
    
    return results[output_cols].reset_index(drop=True)


print("✓ Recommendation function created")

# Test the recommendation function
print("\n" + "-" * 80)
print("Testing recommendation function with sample query...")
print("-" * 80)

test_query = "machine learning datasets"
print(f"\nQuery: '{test_query}'")
print("\nTop 3 Recommendations:")

recommendations = recommend(test_query, top_k=3)
for idx, row in recommendations.iterrows():
    print(f"\n{idx + 1}. {row['title']}")
    print(f"   Similarity Score: {row['similarity_score']:.4f}")
    print(f"   Preview: {row['combined_text'][:100]}...")

# ============================================================================
# 7. SAVE THE MODEL ARTIFACTS
# ============================================================================

print("\n" + "=" * 80)
print("STEP 7: Saving Model Artifacts")
print("=" * 80)

# Create a models directory if it doesn't exist
models_dir = 'models'
os.makedirs(models_dir, exist_ok=True)

# Save the fitted vectorizer
vectorizer_path = os.path.join(models_dir, 'tfidf_vectorizer.pkl')
with open(vectorizer_path, 'wb') as f:
    pickle.dump(vectorizer, f)
print(f"✓ Saved vectorizer to: {vectorizer_path}")

# Save the TF-IDF matrix
tfidf_matrix_path = os.path.join(models_dir, 'tfidf_matrix.pkl')
with open(tfidf_matrix_path, 'wb') as f:
    pickle.dump(tfidf_matrix, f)
print(f"✓ Saved TF-IDF matrix to: {tfidf_matrix_path}")

# Save the processed dataframe (with only necessary columns to save space)
# Select only columns that exist
columns_to_save = []
for col in ['title', 'combined_text', 'score', 'url', 'subreddit.name', 'created_utc']:
    if col in df.columns:
        columns_to_save.append(col)

# Ensure we have at least the essential columns
if 'score' not in df.columns:
    df['score'] = 0
if 'url' not in df.columns:
    df['url'] = ''
if 'created_utc' not in df.columns:
    df['created_utc'] = pd.Timestamp.now().timestamp()
if 'subreddit.name' not in df.columns:
    df['subreddit.name'] = 'unknown'

# Convert created_utc to unix timestamp if it's a datetime string
if df['created_utc'].dtype == 'object':
    print("\nConverting created_utc from datetime string to unix timestamp...")
    try:
        df['created_utc'] = pd.to_datetime(df['created_utc']).astype(int) / 10**9
        print("✓ Converted to unix timestamp")
    except Exception as e:
        print(f"⚠ Warning: Could not convert created_utc: {e}")
        df['created_utc'] = pd.Timestamp.now().timestamp()

# Ensure score is numeric
df['score'] = pd.to_numeric(df['score'], errors='coerce').fillna(0)

columns_to_save = ['title', 'combined_text', 'score', 'url', 'subreddit.name', 'created_utc']
df_to_save = df[columns_to_save].copy()
df_path = os.path.join(models_dir, 'processed_posts.pkl')
with open(df_path, 'wb') as f:
    pickle.dump(df_to_save, f)
print(f"✓ Saved processed dataframe to: {df_path}")

# Save metadata about the model
subreddit_counts = df['subreddit.name'].value_counts().to_dict() if 'subreddit.name' in df.columns else {}

metadata = {
    'num_posts': len(df),
    'num_features': tfidf_matrix.shape[1],
    'vectorizer_params': vectorizer.get_params(),
    'training_date': pd.Timestamp.now().strftime('%Y-%m-%d %H:%M:%S'),
    'max_features': 10000,
    'subreddit_distribution': subreddit_counts,
    'num_subreddits': len(subreddit_counts),
    'recommendation_strategy': 'content-based (subreddit-independent)',
    'description': 'Model trained to recommend based on content similarity, not subreddit matching',
    'data_source': os.path.basename(data_path)
}

metadata_path = os.path.join(models_dir, 'model_metadata.pkl')
with open(metadata_path, 'wb') as f:
    pickle.dump(metadata, f)
print(f"✓ Saved model metadata to: {metadata_path}")

# ============================================================================
# SUMMARY
# ============================================================================

print("\n" + "=" * 80)
print("TRAINING COMPLETE!")
print("=" * 80)

print(f"""
Model Summary:
--------------
✓ Total posts processed: {len(df):,}
✓ Vocabulary size: {tfidf_matrix.shape[1]:,} features
✓ Matrix sparsity: {(1.0 - tfidf_matrix.nnz / (tfidf_matrix.shape[0] * tfidf_matrix.shape[1])) * 100:.2f}%

Saved Artifacts:
----------------
1. {vectorizer_path}
2. {tfidf_matrix_path}
3. {df_path}
4. {metadata_path}

Next Steps:
-----------
1. Load these artifacts in your Flask/FastAPI server
2. Use the recommend() function for inference
3. Consider fine-tuning hyperparameters for better performance

Example Loading Code:
---------------------
import pickle

# Load vectorizer
with open('models/tfidf_vectorizer.pkl', 'rb') as f:
    vectorizer = pickle.load(f)

# Load TF-IDF matrix
with open('models/tfidf_matrix.pkl', 'rb') as f:
    tfidf_matrix = pickle.load(f)

# Load processed posts
with open('models/processed_posts.pkl', 'rb') as f:
    df = pickle.load(f)

# Now you can use the recommend() function!
""")

print("=" * 80)
