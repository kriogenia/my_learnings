use std::io::BufRead;

use clap::Parser;
use common::{file::open_file_or_stdin, CommandClone};

const STDIN_ARG: &str = "-";

#[derive(Debug, Parser)]
pub struct Args {
    #[arg(default_value = STDIN_ARG, help = "List of files to read")]
    files: Vec<String>,
    #[arg(
        short = 'c',
        long = "bytes",
        help = "Number of the first bytes to print"
    )]
    bytes: Option<usize>,
    #[arg(
        default_value = "10",
        short = 'n',
        long = "lines",
        help = "Number of the first lines to print"
    )]
    lines: usize,
}

pub struct Head;

impl CommandClone for Head {
    type Args = Args;

    fn run_with_args(args: Self::Args) -> common::RunResult {
        let multiple_files = args.files.len() > 1;
        for (i, input) in args.files.iter().enumerate() {
            if multiple_files {
                println!("==> {input} <==");
            }

            let buffer = open_file_or_stdin(input)
                .map_err(|msg| format!("Failed to open {input}: {msg}"))?;
            for (line, _) in buffer.lines().zip(0..args.lines) {
                let line = line.map_err(|err| format!("Error reading {input}: {err}"))?;
                println!("{line}");
            }

            if multiple_files && i < args.files.len() - 1 {
                println!();
            }
        }
        Ok(())
    }
}
