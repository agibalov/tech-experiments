FROM ubuntu

RUN apt-get update && \
  apt-get install -y python python-pip && \
  pip install --upgrade pip

RUN mkdir /app

COPY app.py /app/
COPY requirements.txt /app/
WORKDIR /app
RUN pip install -r requirements.txt

ENTRYPOINT ["python"]
CMD ["app.py"]
