import time


def test_count_posts_with_benchmark_or_algorithm(db_connection):
    performance_iterations = 100
    expected_search_count = 18
    cursor = db_connection.cursor()

    fts_query = '''
        SELECT COUNT(*)
        FROM posts_fts
        WHERE posts_fts MATCH 'benchmark OR algorithm'
    '''

    like_query = '''
        SELECT COUNT(*)
        FROM posts
        WHERE title LIKE '%benchmark%'
           OR title LIKE '%algorithm%'
           OR content LIKE '%benchmark%'
           OR content LIKE '%algorithm%'
           OR author LIKE '%benchmark%'
           OR author LIKE '%algorithm%'
    '''

    start_time = time.time()
    for _ in range(performance_iterations):
        cursor.execute(fts_query)
        fts_count = cursor.fetchone()[0]
    fts_duration = time.time() - start_time

    start_time = time.time()
    for _ in range(performance_iterations):
        cursor.execute(like_query)
        like_count = cursor.fetchone()[0]
    like_duration = time.time() - start_time

    assert fts_count == expected_search_count
    assert like_count == expected_search_count
    assert fts_count == like_count

    print(f"\nFTS5 duration ({performance_iterations} queries): {fts_duration:.4f}s")
    print(f"LIKE duration ({performance_iterations} queries): {like_duration:.4f}s")
    print(f"Performance ratio: {like_duration / fts_duration:.1f}x slower")

    assert like_duration >= fts_duration * 50


def test_find_most_recent_posts_with_benchmark_or_algorithm(db_connection):
    performance_iterations = 100
    expected_result_count = 5
    cursor = db_connection.cursor()

    fts_query = '''
        SELECT posts.id, posts.title, posts.created_at
        FROM posts_fts
        JOIN posts ON posts.id = posts_fts.rowid
        WHERE posts_fts MATCH 'benchmark OR algorithm'
        ORDER BY posts.created_at DESC
        LIMIT 5
    '''

    like_query = '''
        SELECT id, title, created_at
        FROM posts
        WHERE title LIKE '%benchmark%'
           OR title LIKE '%algorithm%'
           OR content LIKE '%benchmark%'
           OR content LIKE '%algorithm%'
           OR author LIKE '%benchmark%'
           OR author LIKE '%algorithm%'
        ORDER BY created_at DESC
        LIMIT 5
    '''

    start_time = time.time()
    for _ in range(performance_iterations):
        cursor.execute(fts_query)
        fts_results = cursor.fetchall()
    fts_duration = time.time() - start_time

    start_time = time.time()
    for _ in range(performance_iterations):
        cursor.execute(like_query)
        like_results = cursor.fetchall()
    like_duration = time.time() - start_time

    assert len(fts_results) == expected_result_count
    assert len(like_results) == expected_result_count
    assert len(fts_results) == len(like_results)

    fts_ids = [row[0] for row in fts_results]
    like_ids = [row[0] for row in like_results]
    assert fts_ids == like_ids

    print(f"\nFTS5 duration ({performance_iterations} queries): {fts_duration:.4f}s")
    print(f"LIKE duration ({performance_iterations} queries): {like_duration:.4f}s")
    print(f"Performance ratio: {like_duration / fts_duration:.1f}x slower")

    assert like_duration >= fts_duration * 50


def test_highlight_posts_with_benchmark_or_algorithm(db_connection):
    expected_result_count = 5
    cursor = db_connection.cursor()

    highlight_query = '''
        SELECT posts.id, posts.title, posts.author, posts.created_at,
               highlight(posts_fts, 0, '<mark>', '</mark>') as highlighted_title,
               highlight(posts_fts, 1, '<mark>', '</mark>') as highlighted_content,
               highlight(posts_fts, 2, '<mark>', '</mark>') as highlighted_author
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
        post_id, title, author, created_at, highlighted_title, highlighted_content, highlighted_author = row
        print(f"Post ID: {post_id}")
        print(f"Author: {highlighted_author}")
        print(f"Created: {created_at}")
        print(f"Title: {highlighted_title}")
        print(f"Content snippet: {highlighted_content[:200]}...")
        print("-" * 80)
