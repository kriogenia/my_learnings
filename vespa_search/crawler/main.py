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
        "-l",
        "--limit",
        type=int,
        help="Number of movies to fetch. Omit to fetch all.",
        default=-1,
        required=False,
    )
    parser.add_argument(
        "-o",
        "--output",
        help="Path to output the document files",
        default="../data",
        required=False,
    )
    parser.add_argument(
        "-n",
        "--namespace",
        help="Namespace of the generated documents",
        default="mynamespace",
        required=False,
    )
    parser.add_argument(
        "-d",
        "--doctype",
        help="Doctype of the generated documents",
        default="film",
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
    response = request_tmdb(
        f"/movie/{movie_id}?language=en-US", token
    )  # todo: allow language selection?
    if response.status_code != 200:
        print(f"Failed to get the details of the movie [{movie_id}]")
        return
    return response.text


counter = 0
lock = threading.Lock()
MIN_WAIT = 1.0 / 40.0  # max of forty request per second


def throttle(previous: int) -> int:
    ellapsed = time.time() - previous
    if ellapsed < MIN_WAIT:
        time.sleep(MIN_WAIT - ellapsed)
    return int(time.time())


def movie_fetcher(movie_ids: list[str], token):
    last_request_instant = 0
    for id in movie_ids:
        last_request_instant = throttle(last_request_instant)
        yield id, query_movie(id, token)
        with lock:
            global counter
            counter += 1


def save_movie(movie_id, movie, args):
    vespa_id = f"id:{args.namespace}:{args.doctype}::{movie_id}"
    with open(f"{args.output}/{movie_id}.json", "w") as file:
        file.write(f'{{"put":"{vespa_id}","fields":{movie}}}')


def print_status():
    start_time = time.time()
    while True:
        time.sleep(5)
        ellapsed = time.time() - start_time
        with lock:
            print(f">> Created {counter} movies. Time ellapsed: {ellapsed:.0f} s")


if __name__ == "__main__":
    args = parse_args()
    authenticate(args.token)
    movie_ids = fetch_ids(args.limit)
    threading.Thread(target=print_status)
    for movie_id, movie in movie_fetcher(movie_ids, args.token):
        save_movie(movie_id, movie, args)
    print("> Completed")
