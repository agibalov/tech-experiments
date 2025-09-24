from sqlalchemy import func, literal_column
from tests.sqlalchemy_models import Post, PostFTS, sqlalchemy_session

def test_highlight_posts(sqlalchemy_session):
    expected_result_count = 5
    session = sqlalchemy_session

    results = session.query(
        Post.id,
        Post.title,
        Post.author,
        Post.created_at,
        func.highlight(literal_column('posts_fts'), 0, '<mark>', '</mark>').label('highlighted_title'),
        func.highlight(literal_column('posts_fts'), 1, '<mark>', '</mark>').label('highlighted_content'),
        func.highlight(literal_column('posts_fts'), 2, '<mark>', '</mark>').label('highlighted_author'),
        func.bm25(literal_column('posts_fts')).label('bm25_rank')
    ).join(PostFTS, Post.id == PostFTS.rowid).filter(
        literal_column('posts_fts').op("MATCH")('benchmark OR algorithm')
    ).order_by(Post.created_at.desc()).limit(5).all()

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
