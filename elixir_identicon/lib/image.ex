defmodule Identicon.Image do
  @moduledoc """
    Contains the data structure of the identicon image
  """

  @doc """
    Identicon properties
  """
  defstruct hex: nil, color: nil, grid: nil, rectangles: nil
end
