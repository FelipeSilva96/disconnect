ALTER TABLE Usuario
ADD COLUMN IF NOT EXISTS Localizacao varchar(100),
ADD COLUMN IF NOT EXISTS Hobbies text,
ADD COLUMN IF NOT EXISTS Nivel_experiencia text;