use std::io::BufRead;

use clap::Parser;
use common::{file::open_file_or_stdin, CommandClone, RunResult};

const STDIN_ARG: &str = "-";

#[derive(Debug, Parser)]
#[command(author, version, about)]
pub struct Args {
    #[arg(default_value = STDIN_ARG, help = "List of files to read")]
    files: Vec<String>,
    #[arg(
        default_value = "false",
        short = 'n',
        long = "number",
        help = "Number lines"
    )]
    number_lines: bool,
    #[arg(
        required = false,
        short = 'b',
        long = "number-nonblank",
        help = "Number non-empty lines"
    )]
    number_non_empty_lines: bool,
}

pub struct Cat;

impl CommandClone for Cat {
    type Args = Args;

    fn run_with_args(args: Self::Args) -> RunResult {
        let mut current_line = 0;

        for filename in args.files.iter() {
            let buffer = open_file_or_stdin(filename)
                .map_err(|msg| format!("Failed to open {filename}: {msg}"))?;
            for line in buffer.lines() {
                let line = line.map_err(|err| format!("Error reading {filename}: {err}"))?;

                let prefix = match (args.number_lines, args.number_non_empty_lines) {
                    (_, true) if !line.is_empty() => {
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
