FROM ubuntu:latest
LABEL authors="boris"

ENTRYPOINT ["top", "-b"]