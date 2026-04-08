FROM docker.elastic.co/elasticsearch/elasticsearch:8.15.5

RUN elasticsearch-plugin install analysis-nori
