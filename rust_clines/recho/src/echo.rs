use std::{error::Error, fmt::Display};

use clap::Parser;

#[derive(Debug, Parser)]
#[command(author, version, about)]
struct Args {
    #[arg(required = false, help = "Input text")]
    text: Option<Vec<String>>,
    #[arg(default_value = "false", short = 'n', help = "Do not print newline")]
    omit_newline: bool,
}

pub struct Echo {
    text: String,
}

impl Echo {
    pub fn parse() -> Self {
        let args = Args::parse();
        let text = format!(
            "{}{}",
            args.text.unwrap_or(vec!["".to_owned()]).join(" "),
            if args.omit_newline { "" } else { "\n" }
        );
        Self { text }
    }

    pub fn run(&self) -> Result<(), Box<dyn Error>> {
        print!("{}", self);
        Ok(())
    }
}

impl Display for Echo {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.text)
    }
}
