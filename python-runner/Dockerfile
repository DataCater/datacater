FROM python:3.10.6-slim@sha256:cf85cd32e60184a94d88a0103c289d09024abffaa77680d116d7cc837668ea15 AS build

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    build-essential=12.9 gcc=4:10.2.1-1

WORKDIR /usr/app
RUN python -m venv /usr/app/venv
ENV PATH="/usr/app/venv/bin:$PATH"
COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt


FROM python:3.10.6-slim@sha256:cf85cd32e60184a94d88a0103c289d09024abffaa77680d116d7cc837668ea15

RUN groupadd -g 1001 python && \
    useradd -r -u 1001 -g python python && \
    mkdir /usr/app && \
    chown 1001:1001 /usr/app

WORKDIR /usr/app

COPY --chown=1001:1001 --from=build /usr/app/venv .venv
COPY --chown=1001:1001 . .

USER 1001

ENV PATH="/usr/app/.venv/bin:$PATH"
EXPOSE 50000

CMD [ "python3", "-m", "uvicorn", "runner:app", "--port", "50000" , "--host", "0.0.0.0" ]
