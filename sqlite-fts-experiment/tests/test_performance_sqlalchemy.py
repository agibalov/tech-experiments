import time
from sqlalchemy import or_, literal_column
from tests.sqlalchemy_models import Post, PostFTS, sqlalchemy_session


def test_count_posts(sqlalchemy_session):
    performance_iterations = 100
    expected_search_count = 18
    session = sqlalchemy_session

    start_time = time.time()
    for _ in range(performance_iterations):
        fts_count = session.query(Post).join(PostFTS, Post.id == PostFTS.rowid).filter(
            literal_column('posts_fts').op("MATCH")('benchmark OR algorithm')
        ).count()
    fts_duration = time.time() - start_time

    start_time = time.time()
    for _ in range(performance_iterations):
        like_count = session.query(Post).filter(
            or_(
                Post.title.like('%benchmark%'),
                Post.title.like('%algorithm%'),
                Post.content.like('%benchmark%'),
                Post.content.like('%algorithm%'),
                Post.author.like('%benchmark%'),
                Post.author.like('%algorithm%')
            )
        ).count()
    like_duration = time.time() - start_time

    assert fts_count == expected_search_count
    assert like_count == expected_search_count
    assert fts_count == like_count

    print(f"\nFTS5 duration ({performance_iterations} queries): {fts_duration:.4f}s")
    print(f"LIKE duration ({performance_iterations} queries): {like_duration:.4f}s")
    print(f"Performance ratio: {like_duration / fts_duration:.1f}x slower")

    assert like_duration >= fts_duration * 10


def test_find_most_recent_posts(sqlalchemy_session):
    performance_iterations = 100
    expected_result_count = 5
    session = sqlalchemy_session

    start_time = time.time()
    for _ in range(performance_iterations):
        fts_results = session.query(
            Post.id, Post.title, Post.created_at
        ).join(PostFTS, Post.id == PostFTS.rowid).filter(
            literal_column('posts_fts').op("MATCH")('benchmark OR algorithm')
        ).order_by(Post.created_at.desc()).limit(5).all()
    fts_duration = time.time() - start_time

    start_time = time.time()
    for _ in range(performance_iterations):
        like_results = session.query(
            Post.id, Post.title, Post.created_at
        ).filter(
            or_(
                Post.title.like('%benchmark%'),
                Post.title.like('%algorithm%'),
                Post.content.like('%benchmark%'),
                Post.content.like('%algorithm%'),
                Post.author.like('%benchmark%'),
                Post.author.like('%algorithm%')
            )
        ).order_by(Post.created_at.desc()).limit(5).all()
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

    assert like_duration >= fts_duration * 10
