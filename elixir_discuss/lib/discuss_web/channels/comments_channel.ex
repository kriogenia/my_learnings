defmodule DiscussWeb.CommentsChannel do
  use DiscussWeb, :channel

  alias Discuss.Forum

  @impl true
  def join("comments:" <> topic_id, payload, socket) do
    if authorized?(payload) do
	  topic = String.to_integer(topic_id)
	  |> Forum.get_topic_with_comments!

      {:ok, %{comments: topic.comments}, assign(socket, :topic, topic)}
    else
      {:error, %{reason: "unauthorized"}}
    end
  end

  def handle_in("comments:add", %{"content" => content} = payload, socket) do
	case socket.assigns.topic |> Forum.add_comment(content, socket.assigns.user) do
	  {:ok, comment} -> 
		broadcast!(socket, "comments:#{socket.assigns.topic.id}:new", %{comment: comment})
		{:reply, {:ok, payload}, socket}
	  {:error, reason} -> {:reply, {:error, %{errors: reason}}, socket}
	end
  end

  # Channels can be used in a request/response fashion
  # by sending replies to requests from the client
  @impl true
  def handle_in("ping", payload, socket) do
    {:reply, {:ok, payload}, socket}
  end

  # It is also common to receive messages from the client and
  # broadcast to everyone in the current topic (comments:lobby).
  @impl true
  def handle_in("shout", payload, socket) do
    broadcast(socket, "shout", payload)
    {:noreply, socket}
  end

  # Add authorization logic here as required.
  defp authorized?(_payload) do
    true
  end
end
