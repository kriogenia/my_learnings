# TMDB Crawler

This crawler queries the TMDB API to generate a list of movie documents to load into the database. In order to be used,
you only need to pass a valid TMDB API token, [see](https://developer.themoviedb.org/v4/docs/getting-started).

```sh
python main.py -t <your_token>
```

> WIP: right now the crawler only authenticates in the API