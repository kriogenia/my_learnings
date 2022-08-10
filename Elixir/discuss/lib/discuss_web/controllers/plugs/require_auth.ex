defmodule Discuss.Plugs.RequireAuth do
	import Phoenix.Controller
	
	alias DiscussWeb.Router.Helpers, as: Routes

	def init(_params) do
	end

	def call(conn, _params) do
		if conn.assigns[:user] do
			conn
		else
			conn
			|> put_flash(:error, "You need to be authenticated to perform this action")
			|> redirect(to: Routes.topic_path(conn, :index))
			|> Plug.Conn.halt()
		end
	end

end