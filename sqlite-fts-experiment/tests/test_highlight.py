def test_highlight_posts_with_benchmark_or_algorithm(db_connection):
    expected_result_count = 5
    cursor = db_connection.cursor()

    highlight_query = '''
        SELECT posts.id, posts.title, posts.author, posts.created_at,
               highlight(posts_fts, 0, '<mark>', '</mark>') as highlighted_title,
               highlight(posts_fts, 1, '<mark>', '</mark>') as highlighted_content,
               highlight(posts_fts, 2, '<mark>', '</mark>') as highlighted_author,
               bm25(posts_fts) as bm25_rank
        FROM posts_fts
        JOIN posts ON posts.id = posts_fts.rowid
        WHERE posts_fts MATCH 'benchmark OR algorithm'
        ORDER BY posts.created_at DESC
        LIMIT 5
    '''

    cursor.execute(highlight_query)
    results = cursor.fetchall()

    assert len(results) == expected_result_count

    print(f"\nFound {len(results)} posts with highlighted snippets:")
    print("=" * 80)

    for row in results:
        post_id, title, author, created_at, highlighted_title, highlighted_content, highlighted_author, bm25_rank = row
        print(f"Post ID: {post_id}")
        print(f"Author: {highlighted_author}")
        print(f"Created: {created_at}")
        print(f"BM25 Rank: {bm25_rank:.4f}")
        print(f"Title: {highlighted_title}")
        print(f"Content snippet: {highlighted_content[:200]}...")
        print("-" * 80)
