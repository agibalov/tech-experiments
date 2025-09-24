import sqlite3
import random
import os
import pytest
import time
from faker import Faker

DB_FILE = 'test_posts.db'
RANDOM_SEED = 42


@pytest.fixture
def populated_database():
    random.seed(RANDOM_SEED)
    fake = Faker()
    fake.seed_instance(RANDOM_SEED)

    if os.path.exists(DB_FILE):
        os.remove(DB_FILE)

    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()

    cursor.execute('''
        CREATE TABLE posts (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            content TEXT NOT NULL,
            author TEXT NOT NULL,
            created_at TEXT NOT NULL
        )
    ''')

    cursor.execute('''
        CREATE VIRTUAL TABLE posts_fts USING fts5(
            title,
            content,
            author,
            content=posts,
            content_rowid=id
        )
    ''')

    cursor.execute('''
        CREATE TRIGGER posts_fts_insert AFTER INSERT ON posts BEGIN
            INSERT INTO posts_fts(rowid, title, content, author)
            VALUES (new.id, new.title, new.content, new.author);
        END
    ''')

    cursor.execute('''
        CREATE TRIGGER posts_fts_delete AFTER DELETE ON posts BEGIN
            INSERT INTO posts_fts(posts_fts, rowid, title, content, author)
            VALUES('delete', old.id, old.title, old.content, old.author);
        END
    ''')

    cursor.execute('''
        CREATE TRIGGER posts_fts_update AFTER UPDATE ON posts BEGIN
            INSERT INTO posts_fts(posts_fts, rowid, title, content, author)
            VALUES('delete', old.id, old.title, old.content, old.author);
            INSERT INTO posts_fts(rowid, title, content, author)
            VALUES (new.id, new.title, new.content, new.author);
        END
    ''')

    for i in range(1000):
        title_words = random.randint(3, 10)
        title = fake.catch_phrase()

        while len(title.split()) < title_words:
            title += " " + fake.word().capitalize()

        title_parts = title.split()
        if len(title_parts) > title_words:
            title = " ".join(title_parts[:title_words])

        num_paragraphs = random.randint(3, 10)
        paragraphs = []

        for _ in range(num_paragraphs):
            num_sentences = random.randint(2, 10)
            sentences = []

            for _ in range(num_sentences):
                sentence = fake.sentence(nb_words=random.randint(5, 15))
                sentences.append(sentence)

            paragraph = " ".join(sentences)
            paragraphs.append(paragraph)

        content = "\n\n".join(paragraphs)

        author = fake.name()

        created_at = fake.date_time_between(start_date='-1y', end_date='now').isoformat()

        cursor.execute('''
            INSERT INTO posts (title, content, author, created_at)
            VALUES (?, ?, ?, ?)
        ''', (title, content, author, created_at))

    conn.commit()
    conn.close()


@pytest.fixture
def db_connection(populated_database):
    conn = sqlite3.connect(DB_FILE)
    yield conn
    conn.close()


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
