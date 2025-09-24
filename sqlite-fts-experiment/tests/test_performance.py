import time


def test_count_posts(db_connection):
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


def test_find_most_recent_posts(db_connection):
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
