"""
Quick Test Script for Recommendation API
=========================================
Tests both legacy and new API formats.
"""

import requests
import json

BASE_URL = "http://localhost:5000/api"

def test_legacy_get():
    """Test legacy GET request format."""
    print("\n" + "="*80)
    print("TEST 1: Legacy GET Request (backward compatibility)")
    print("="*80)
    
    url = f"{BASE_URL}/recommend"
    params = {
        'q': 'technology news',
        'top_k': 5,
        'min_score': 0.0
    }
    
    response = requests.get(url, params=params)
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    
    return response.status_code == 200


def test_new_post():
    """Test new POST request format with history."""
    print("\n" + "="*80)
    print("TEST 2: New POST Request (history-based)")
    print("="*80)
    
    url = f"{BASE_URL}/recommend"
    data = {
        'history_contents': [
            'artificial intelligence and machine learning',
            'python programming tutorials',
            'data science projects'
        ],
        'top_k': 5,
        'min_score': 0.0
    }
    
    response = requests.post(url, json=data)
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    
    return response.status_code == 200


def test_health():
    """Test health check endpoint."""
    print("\n" + "="*80)
    print("TEST 0: Health Check")
    print("="*80)
    
    url = f"{BASE_URL}/health"
    response = requests.get(url)
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    
    return response.status_code == 200


def test_model_info():
    """Test model info endpoint."""
    print("\n" + "="*80)
    print("TEST 3: Model Info")
    print("="*80)
    
    url = f"{BASE_URL}/model/info"
    response = requests.get(url)
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    
    return response.status_code == 200


if __name__ == "__main__":
    print("\n" + "#"*80)
    print("# RECOMMENDATION API TEST SUITE")
    print("#"*80)
    
    results = {
        'Health Check': test_health(),
        'Legacy GET': test_legacy_get(),
        'New POST': test_new_post(),
        'Model Info': test_model_info()
    }
    
    print("\n" + "="*80)
    print("TEST RESULTS")
    print("="*80)
    for test_name, passed in results.items():
        status = "✓ PASS" if passed else "✗ FAIL"
        print(f"{test_name}: {status}")
    
    all_passed = all(results.values())
    print("\n" + ("="*80))
    print(f"Overall: {'ALL TESTS PASSED ✓' if all_passed else 'SOME TESTS FAILED ✗'}")
    print("="*80 + "\n")
