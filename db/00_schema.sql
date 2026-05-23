-- DB スキーマ定義
-- tokkun-sql と同じテーブル構造を使用する

CREATE TABLE departments (
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    location VARCHAR(100)
);

CREATE TABLE employees (
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    dept_id    INTEGER REFERENCES departments(id),
    salary     INTEGER,
    hire_date  DATE,
    manager_id INTEGER REFERENCES employees(id)
);

CREATE TABLE projects (
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    start_date DATE,
    end_date   DATE,
    budget     INTEGER
);

CREATE TABLE employee_projects (
    employee_id INTEGER NOT NULL REFERENCES employees(id),
    project_id  INTEGER NOT NULL REFERENCES projects(id),
    role        VARCHAR(100),
    PRIMARY KEY (employee_id, project_id)
);
