import argparse
import requests


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
        headers={"accept": "application/json", "Authorization": f"Bearer {token}"}
    )


def authenticate(token: str):
    print("> Authenticating to TMDB API")
    auth_response = request_tmdb("/authentication", token)
    assert auth_response.status_code == 200, "Error while authenticating to TMDB, please check the token"
    print("> Authentication successful")


if __name__ == "__main__":
    args = parse_args()
    authenticate(args.token)
