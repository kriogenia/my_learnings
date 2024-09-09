import argparse
import gzip
import requests
import threading
import time
from datetime import date, timedelta


def parse_args():
    parser = argparse.ArgumentParser(
        prog="TMDB Crawler",
        description="Tool to automatically query TMDB API /movies to populate with data the provided list and transform it to the format accepted by the Index API",
    )
    parser.add_argument(
        "-t", "--token", help="Your TMDB API token", default=None, required=True
    )
    parser.add_argument(
        "-n", "--number", type=int, help="Number of movies to fetch. Omit to fetch all.", default=-1, required=False
    )
    parser.add_argument(
        "-o",
        "--output",
        help="Path to output the document files",
        default=None,
        required=False,
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


def fetch_export():
    yesterday = date.today() - timedelta(days=1)
    export_date = yesterday.strftime("%m_%d_%Y")
    with requests.get(
        f"http://files.tmdb.org/p/exports/movie_ids_{export_date}.json.gz", stream=True
    ) as res:
        return gzip.decompress(res.content).decode()


def fetch_ids(n: int):
    export = fetch_export()
    is_limit_reached = lambda i: i == n if n >= 0 else lambda _: False
    ids = []
    for i, line in enumerate(export.splitlines()):
        if is_limit_reached(i):
            break
        ids.append(line[20:].split(",", 1)[0])
    print(f"> Fetched the IDs of {len(ids)} movies")
    return ids


def query_movie(movie_id: str, token: str):
    response = request_tmdb(f"/movie/{movie_id}?language=en-US", token)	# todo: allow language selection?
    if response.status_code != 200:
        print(f"Failed to get the details of the movie [{movie_id}]")
        return
    return response.text


def print_status():
    start_time = time.time()
    while True:
        time.sleep(5)
        ellapsed = time.time() - start_time
        print(f">> Requested: {counter} movies. Time ellapsed: {ellapsed:.0f} s")


if __name__ == "__main__":
    args = parse_args()
    authenticate(args.token)
    movie_ids = fetch_ids(args.number)
    # todo: move to function
    for movie_id in movie_ids:
        movie = query_movie(movie_id, args.token)
        print(movie)
        # todo: add wait
        counter += 1
    threading._start_new_thread(print_status, ())
