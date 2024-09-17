# TMDB Crawler

This crawler queries the TMDB API to generate a list of movie documents to load into the database. In order to be used,
you only need to pass a valid TMDB API token, [see](https://developer.themoviedb.org/v4/docs/getting-started).

It performs this action reading the last [Daily ID Export](https://developer.themoviedb.org/docs/daily-id-exports) and
querying the details of each movie one by one at a rate of 40 movies per second. By default it will attempt to pull
all the movies of the exported file.

```sh
python main.py -t <your_token>
```
Unless you want to get the full dataset, it's advised to pass a limit of movies, it will request as much as you specify
in the order they are read in the export file. For example to retrieve 100 movies:

```sh
python main.py -t <your_token> -l 100
```

Other supported options allow to specify the destiny folder of the pulled movies or the namespace and doctype to set while
creating the Vespa document IDs.