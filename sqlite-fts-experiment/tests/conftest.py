import sqlite3
import random
import os
import pytest
from faker import Faker

DB_FILE = 'test_posts.db'


@pytest.fixture
def populated_database():
    random_seed = 42
    random.seed(random_seed)
    fake = Faker()
    fake.seed_instance(random_seed)

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
