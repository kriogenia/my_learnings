defmodule DiscussWeb.TopicController do
  use DiscussWeb, :controller

  alias Discuss.Forum
  alias Discuss.Forum.Topic

  plug Discuss.Plugs.RequireAuth when action in [
	:new, :create, :edit, :update, :delete
  ] 

  plug :check_post_owner when action in [
	:edit, :update, :delete
  ]

  def index(conn, _params) do
    topics = Forum.list_topics()
    render(conn, "index.html", topics: topics)
  end

  def new(conn, _params) do
    changeset = Forum.change_topic(%Topic{})
    render(conn, "new.html", changeset: changeset)
  end

  def create(conn, %{"topic" => topic_params}) do
	case Forum.create_topic(conn.assigns.user, topic_params) do
      {:ok, topic} ->
        conn
        |> put_flash(:info, "Topic created successfully.")
        |> redirect(to: Routes.topic_path(conn, :show, topic))

      {:error, %Ecto.Changeset{} = changeset} ->
        render(conn, "new.html", changeset: changeset)
    end
  end

  def show(conn, %{"id" => id}) do
    topic = Forum.get_topic!(id)
    render(conn, "show.html", topic: topic)
  end

  def edit(conn, %{"id" => id}) do
    topic = Forum.get_topic!(id)
    changeset = Forum.change_topic(topic)
    render(conn, "edit.html", topic: topic, changeset: changeset)
  end

  def update(conn, %{"id" => id, "topic" => topic_params}) do
    topic = Forum.get_topic!(id)

    case Forum.update_topic(topic, topic_params) do
      {:ok, topic} ->
        conn
        |> put_flash(:info, "Topic updated successfully.")
        |> redirect(to: Routes.topic_path(conn, :show, topic))

      {:error, %Ecto.Changeset{} = changeset} ->
        render(conn, "edit.html", topic: topic, changeset: changeset)
    end
  end

  def delete(conn, %{"id" => id}) do
    topic = Forum.get_topic!(id)
    {:ok, _topic} = Forum.delete_topic(topic)

    conn
    |> put_flash(:info, "Topic deleted successfully.")
    |> redirect(to: Routes.topic_path(conn, :index))
  end

  def check_post_owner(%{ params: %{ "id" => topic_id }} = conn, _params) do
	if Forum.get_topic!(topic_id).user_id == conn.assigns.user.id do
		conn
	else
		conn
		|> put_flash(:error, "You don't have permissions to do perform this action")
		|> redirect(to: Routes.topic_path(conn, :index))
		|> halt()
	end
  end

end
