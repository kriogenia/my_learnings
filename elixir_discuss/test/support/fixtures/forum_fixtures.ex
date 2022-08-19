defmodule Discuss.ForumFixtures do
  @moduledoc """
  This module defines test helpers for creating
  entities via the `Discuss.Forum` context.
  """

  @doc """
  Generate a topic.
  """
  def topic_fixture(attrs \\ %{}) do
    {:ok, topic} =
      attrs
      |> Enum.into(%{
        title: "some title"
      })
      |> Discuss.Forum.create_topic()

    topic
  end
end
