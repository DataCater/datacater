#!/bin/env python3

import sys

import requests


if __name__ == "__main__":
  response = requests.get("http://localhost:50000/health")
  if response.status_code < 300:
    print("System is alive")
  else:
    print("System is dead")
    sys.exit(1)
