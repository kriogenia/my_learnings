defmodule Discuss.Repo.Migrations.CreateComments do
  use Ecto.Migration

  def change do
    create table(:comments) do
      add :content, :string
      add :user_id, references(:users, on_delete: :nothing)
      add :topic_id, references(:topics, on_delete: :delete_all)

      timestamps()
    end

    create index(:comments, [:user_id])
    create index(:comments, [:topic_id])
  end
end
