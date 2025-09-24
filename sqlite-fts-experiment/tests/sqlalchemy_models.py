from sqlalchemy import create_engine, Column, Integer, String
from sqlalchemy.orm import declarative_base, sessionmaker
import pytest
from .conftest import DB_FILE, populated_database

Base = declarative_base()


class Post(Base):
    __tablename__ = 'posts'

    id = Column(Integer, primary_key=True, autoincrement=True)
    title = Column(String, nullable=False)
    content = Column(String, nullable=False)
    author = Column(String, nullable=False)
    created_at = Column(String, nullable=False)


class PostFTS(Base):
    __tablename__ = 'posts_fts'

    rowid = Column(Integer, primary_key=True)
    title = Column(String)
    content = Column(String)
    author = Column(String)


@pytest.fixture
def sqlalchemy_session(populated_database):
    engine = create_engine(f'sqlite:///{DB_FILE}')
    Session = sessionmaker(bind=engine)
    session = Session()
    yield session
    session.close()
