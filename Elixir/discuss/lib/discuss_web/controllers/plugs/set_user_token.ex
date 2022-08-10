defmodule Discuss.Plugs.SetUserToken do
	import Plug.Conn

	def init(_params) do
	end

	def call(conn, _params) do
	  if user = conn.assigns[:user] do
	    token = Phoenix.Token.sign(conn, "user socket", user.id)
	    assign(conn, :user_token, token)
	  else
	    conn
	  end
	end

end