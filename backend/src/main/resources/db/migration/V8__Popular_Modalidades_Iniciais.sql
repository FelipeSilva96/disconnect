INSERT INTO Modalidade (Id_categoria, Nome, Categoria)
VALUES
    (1, 'Futebol', 'Esportes'),
    (2, 'Futsal', 'Esportes'),
    (3, 'Volei', 'Esportes'),
    (4, 'Tenis', 'Esportes'),
    (5, 'Basquete', 'Esportes'),
    (6, 'Corrida', 'Esportes'),
    (7, 'Trilha', 'Natureza'),
    (8, 'Grupo de Estudo', 'Estudos'),
    (9, 'Monitoria', 'Estudos'),
    (10, 'Board Games', 'Jogos'),
    (11, 'Aula de Canto', 'Musica'),
    (12, 'Pintura', 'Arte')
ON CONFLICT (Id_categoria) DO NOTHING;

SELECT setval(
    pg_get_serial_sequence('modalidade', 'id_categoria'),
    COALESCE((SELECT MAX(Id_categoria) FROM Modalidade), 1),
    true
);