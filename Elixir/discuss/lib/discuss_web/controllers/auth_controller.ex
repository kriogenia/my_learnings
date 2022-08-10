defmodule DiscussWeb.AuthController do
	use DiscussWeb, :controller
	plug Ueberauth

	alias Discuss.Repo
	alias Discuss.User
  
	def callback(%{ assigns:  %{ ueberauth_auth: auth } } = conn, params) do
	  user_params = %{ token: auth.credentials.token, email: auth.info.email, provider: "github" }
	  signin(conn, User.changeset(%User{}, user_params))
	end

	def signout(conn, _params) do
	  conn
	  |> configure_session(drop: true)
	  |> redirect(to: Routes.topic_path(conn, :index))
	end

	defp signin(conn, changeset) do
	  case User.insert_or_update(changeset) do
		{ :ok, user } ->
		  conn
			|> put_flash(:info, "Welcome to Discuss: #{user.email}")
			|> put_session(:user_id, user.id)
			|> redirect(to: Routes.topic_path(conn, :index))
		{ :error, _reason } ->
		  Logger.error("Authentication failed")
		  conn
		  |> put_flash(:error, "Error with your authentication")
		  |> redirect(to: Routes.topic_path(conn, :index))
	  end
	end
  
  end
  