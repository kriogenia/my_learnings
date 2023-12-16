use std::{
    fs::File,
    io::{BufRead, BufReader, Read},
};

use clap::Parser;
use common::{CommandClone, RunResult};

#[derive(Debug, Parser)]
#[command(author, version, about)]
pub struct Args {
    #[arg(required = true, help = "List of files to read")]
    files: Vec<String>,
    #[arg(default_value = "false", short = 'n', help = "Number lines")]
    number_lines: bool,
    #[arg(required = false, short = 'r', help = "Number non-empty lines")]
    number_non_empty_lines: bool,
}

pub struct Cat;

impl CommandClone<Args> for Cat {
    fn run_with_args(args: Args) -> RunResult {
        for filename in args.files.iter() {
            let buffer =
                open(filename).map_err(|msg| format!("Failed to open {filename}: {msg}"))?;
            for line in buffer.lines() {
                println!(
                    "{}",
                    line.map_err(|err| format!("Error reading {filename}: {err}"))?
                )
            }
        }
        Ok(())
    }
}

fn open(filename: &str) -> Result<Box<dyn BufRead>, String> {
    match File::open(filename) {
        Ok(file) => Ok(Box::new(BufReader::new(file))),
        Err(error) => Err(format!("{}", error)),
    }
}
