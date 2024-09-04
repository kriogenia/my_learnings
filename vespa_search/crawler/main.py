import argparse
import requests
import threading
import time


def parse_args():
    parser = argparse.ArgumentParser(
        prog="TMDB Crawler",
        description="Tool to automatically query TMDB API /movies to populate with data the provided list and transform it to the format accepted by the Index API",
    )
    parser.add_argument(
        "-t", "--token", help="Your TMDB API token", default=None, required=True
    )
    return parser.parse_args()


def request_tmdb(path: str, token: str):
    return requests.get(
        url=f"https://api.themoviedb.org/3{path}",
        headers={"accept": "application/json", "Authorization": f"Bearer {token}"},
    )


def authenticate(token: str):
    print("> Authenticating to TMDB API")
    auth_response = request_tmdb("/authentication", token)
    assert (
        auth_response.status_code == 200
    ), "Error while authenticating to TMDB, please check the token"
    print("> Authentication successful")


counter = 0


def print_status():
    start_time = time.time()
    while True:
        time.sleep(5)
        ellapsed = time.time() - start_time
        print(f">> Requested: {counter} movies. Time ellapsed: {ellapsed:.0f} s")


if __name__ == "__main__":
    args = parse_args()
    authenticate(args.token)
    threading._start_new_thread(print_status, ())
