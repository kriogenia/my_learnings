use std::{
    fs::File,
    io::{BufRead, BufReader},
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
    #[arg(required = false, short = 'b', help = "Number non-empty lines")]
    number_non_empty_lines: bool,
}

pub struct Cat;

impl CommandClone<Args> for Cat {
    fn run_with_args(args: Args) -> RunResult {
        let mut current_line = 0;

        for filename in args.files.iter() {
            let buffer =
                open(filename).map_err(|msg| format!("Failed to open {filename}: {msg}"))?;
            for line in buffer.lines() {
                let line = line.map_err(|err| format!("Error reading {filename}: {err}"))?;

                let prefix = match (args.number_lines, args.number_non_empty_lines) {
                    (_, true) if line.is_empty() => {
                        current_line += 1;
                        format!("{current_line:>6}\t")
                    }
                    (true, false) => {
                        current_line += 1;
                        format!("{current_line:>6}\t")
                    }
                    (_, _) => String::new(),
                };

                println!("{prefix}{line}")
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
