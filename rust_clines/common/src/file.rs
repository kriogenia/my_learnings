use std::{
    fs::File,
    io::{self, BufRead, BufReader},
};

pub const STDIN_ARG: &str = "-";

pub fn open_file_or_stdin(filename: &str) -> Result<Box<dyn BufRead>, String> {
    if filename == STDIN_ARG {
        return Ok(Box::new(BufReader::new(io::stdin())));
    }

    match File::open(filename) {
        Ok(file) => Ok(Box::new(BufReader::new(file))),
        Err(error) => Err(format!("{}", error)),
    }
}
