defmodule Discuss.User do
  use Ecto.Schema
  import Ecto.Changeset

  alias Discuss.Repo

  @derive {Jason.Encoder, only: [:email]}

  schema "users" do
    field :email, :string
    field :provider, :string
    field :token, :string
	has_many :topics, Discuss.Forum.Topic
	has_many :comments, Discuss.Forum.Comment

    timestamps()
  end

  @doc false
  def changeset(user, attrs) do
    user
    |> cast(attrs, [:email, :provider, :token])
    |> validate_required([:email, :provider, :token])
  end

  def insert_or_update(user) do
	case Repo.get_by(Discuss.User, email: user.changes.email) do
	  nil -> Repo.insert(user)
	  user -> { :ok, user } 
	end
  end

end
