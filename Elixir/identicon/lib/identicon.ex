defmodule Identicon do
  @moduledoc """
    Collection of functions to generate an identicon from a string
  """

  @doc """
    Generates an identicon from the given string
  """
  def main(input) do
    input
    |> hash_input
    |> pick_color
    |> build_grid
    |> filter_odd_squares
    |> build_pixel_map
    |> draw_image
    |> save_image(input)
  end

  @doc """
    Generates a list of numbers from a given string

    ## Examples

      iex> Identicon.hash_input("foo")
      %Identicon.Image{hex: [172, 189, 24, 219, 76, 194, 248, 92, 237, 239, 101, 79, 204, 196, 164, 216]}

  """
  def hash_input(input) do
    hex = :crypto.hash(:md5, input) |> :binary.bin_to_list
    %Identicon.Image{ hex: hex }
  end

  @doc """
    Picks the color of the identicon from the image hex values

    ## Examples

      iex> image = Identicon.hash_input("foo")
      iex> Identicon.pick_color(image)
      %Identicon.Image{
        color: {172, 189, 24},
        hex: [172, 189, 24, 219, 76, 194, 248, 92, 237, 239, 101, 79, 204, 196, 164, 216]
      }

  """
  def pick_color(%Identicon.Image{ hex: [ r, g, b | _tail ] } = image) do
    %Identicon.Image{ image | color: { r, g, b }}
  end

  @doc """
    Builds the grid of the specified image based on its hex values

    ## Examples

      iex> image = %Identicon.Image{ hex: [1, 2, 3, 4 ]}
      iex> Identicon.build_grid(image)
      %Identicon.Image{
        hex: [1, 2, 3, 4 ],
        grid: [{1, 0}, {2, 1}, {3, 2}, {2, 3}, {1, 4}]
      }

  """
  def build_grid(%Identicon.Image{ hex: hex } = image) do
    grid =
      hex
      |> Enum.chunk_every(3, 3, :discard)
      |> Enum.map(&mirror_row/1)
      |> List.flatten
      |> Enum.with_index
    %Identicon.Image{ image | grid: grid }
  end

  @doc """
    Mirrors a row of five values, center column is not duplicated

    ## Examples

      iex> Identicon.mirror_row([1, 2, 3])
      [1, 2, 3, 2, 1]

  """
  def mirror_row([ first, second | _center ] = row) do
    row ++ [ second, first ]
  end

  @doc """
    Takes an image with a grid and takes out the odd squares of it

    ## Examples

      iex> image = %Identicon.Image{ grid: [ { 1, 0 }, { 2, 1 } ]}
      iex> %Identicon.Image{ grid: grid} = Identicon.filter_odd_squares(image)
      iex> grid
      [{2, 1}]

  """
  def filter_odd_squares(%Identicon.Image{ grid: grid } = image) do
    grid = Enum.filter grid, fn({ value, _index }) -> rem(value, 2) == 0 end
    %Identicon.Image{ image | grid: grid }
  end

  @doc """
    Takes an image with a grid and generates its pixel map

    ## Examples

      iex> image = %Identicon.Image{ grid: [{ 1, 0 }, { 2, 1 }] }
      iex> %Identicon.Image{ rectangles: rectangles } = Identicon.build_pixel_map(image)
      iex> rectangles
      [{{ 0, 0 }, { 50, 50 }}, {{ 50, 0 }, { 100, 50 }}]

  """
  def build_pixel_map(%Identicon.Image{ grid: grid } = image) do
    rectangles =
      grid
      |> Enum.map(&get_rectangle_pixels/1)
    %Identicon.Image{ image | rectangles: rectangles }
  end

  @doc """
    Calculate the rectangle of the given index based on the length and size

    # Examples

      iex> Identicon.get_rectangle_pixels({ 3, 7 }, 5, 20)
      {{ 40, 20 }, { 60, 40 }}

  """
  def get_rectangle_pixels({ _value, index }, length \\ 5, size \\ 50) do
    x = rem(index, length) * size
    y = div(index, length) * size
    { { x, y }, { x + size, y + size }}
  end

  @doc """
    Generates the binary of the passed image ready to be saved
  """
  def draw_image(%Identicon.Image{ color: color, rectangles: rectangles }) do
    image = :egd.create(250, 250)
    fill = :egd.color(color)

    Enum.each rectangles, fn({ from, to }) ->
      :egd.filledRectangle(image, from, to, fill)
    end

    :egd.render(image)
  end

  @doc """
    Stores the `image` in the specified `path`
  """
  def save_image(image, path) do
    File.write("#{path}.png", image)
  end

end
