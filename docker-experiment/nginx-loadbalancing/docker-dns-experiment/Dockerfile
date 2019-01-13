FROM node
ADD ./app /app
WORKDIR /app
RUN npm install
ENTRYPOINT ["node", "app.js"]
EXPOSE 1153
