defmodule Cards do
  @moduledoc """
  Functions to generating and manage a deck of cards
  """

  @doc """
  Creates a deck of card.

  ## Examples

      iex> Cards.create_deck()
      [
        diamonds: :ace,
        diamonds: :two,
        diamonds: :three,
        diamonds: :four,
        diamonds: :five,
        diamonds: :six,
        diamonds: :seven,
        diamonds: :eight,
        diamonds: :nine,
        diamonds: :ten,
        diamonds: :jack,
        diamonds: :queen,
        diamonds: :king,
        spades: :ace,
        spades: :two,
        spades: :three,
        spades: :four,
        spades: :five,
        spades: :six,
        spades: :seven,
        spades: :eight,
        spades: :nine,
        spades: :ten,
        spades: :jack,
        spades: :queen,
        spades: :king,
        clubs: :ace,
        clubs: :two,
        clubs: :three,
        clubs: :four,
        clubs: :five,
        clubs: :six,
        clubs: :seven,
        clubs: :eight,
        clubs: :nine,
        clubs: :ten,
        clubs: :jack,
        clubs: :queen,
        clubs: :king,
        hearts: :ace,
        hearts: :two,
        hearts: :three,
        hearts: :four,
        hearts: :five,
        hearts: :six,
        hearts: :seven,
        hearts: :eight,
        hearts: :nine,
        hearts: :ten,
        hearts: :jack,
        hearts: :queen,
        hearts: :king
      ]

  """
  def create_deck do
    values = [ :ace, :two, :three, :four, :five, :six, :seven, :eight, :nine, :ten, :jack, :queen, :king ]
    suits = [ :diamonds, :spades, :clubs, :hearts ]

    for suit <- suits, value <- values do
      { suit, value }
    end
  end

  @doc """
  Checks if the deck contains the specified card

  ## Examples

    iex > deck = Cards.create_deck()
    iex > Cards.contains?(deck, { :diamond, :ace })
    true

  """
  def contains?(deck, card) do
    Enum.member?(deck, card)
  end

  @doc """
  Shuffles the content of the deck
  """
  def shuffle(deck) do
     Enum.shuffle(deck)
  end

  @doc """
  Generates a hand of the specified `quantity` and returns its content and the remainder of the deck.

  ## Examples

    iex> deck = Cards.create_deck()
    iex> { hand, deck } = Cards.deal(deck, 2)
    iex> hand
    [diamonds: :ace, diamonds: :two]
    iex> length(deck)
    50

  """
  def deal(deck, quantity) do
    Enum.split(deck, quantity)
  end

  @doc """
  Saves the deck in the local file system with the specified `file_name`
  """
  def save(deck, file_name) do
    binary = :erlang.term_to_binary(deck)
    File.write(file_name, binary)
  end

  @doc """
  Loads the deck from the file named `file_name` from the local file system
  Returns `:ok` and the deck if the file exists and `:error` in case it does not.
  """
  def load(file_name) do
    case File.read(file_name) do
      { :ok, binary } -> { :ok, :erlang.binary_to_term(binary) }
      { :error, _ } -> { :error, :enoent }
    end
  end

  @doc """
  Creates a deck, shuffles it and deal a hand of the specified size
  """
  def create_hand(hand_size) do
    create_deck() |> shuffle() |> deal(hand_size)
  end

end
