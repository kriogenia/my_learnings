defmodule CardsTest do
  use ExUnit.Case
  doctest Cards

  test "create the deck" do
    deck = Cards.create_deck()
    assert length(deck) == 52
  end

  test "contain card" do
    deck = Cards.create_deck()
    assert Cards.contains?(deck, { :diamonds, :ace }) == true
  end

  test "does not contain card" do
    deck = Cards.create_deck()
    assert Cards.contains?(deck, {:oro, :sota }) == false
  end

  test "shuffle the deck" do
    deck = Cards.create_deck()
    shuffled = Cards.shuffle(deck)
    assert deck != shuffled
    assert length(deck) == length(shuffled)
  end

  test "deal a hand" do
    deck = Cards.create_deck()
    { hand, remaining } = Cards.deal(deck, 5)
    assert length(hand) == 5
    assert length(remaining) == 47
  end

  test "create a hand" do
    { hand, remaining } = Cards.create_hand(5)
    assert length(hand) == 5
    assert length(remaining) == 47
  end

end
