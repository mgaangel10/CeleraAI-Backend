

INSERT INTO usuario (id, name,last_name, username, email, password,phone_number,ACCOUNT_NON_EXPIRED,ACCOUNT_NON_LOCKED,CREDENTIALS_NON_EXPIRED,enabled,foto_url) VALUES ('5d818565-99f9-4d80-920e-8259c6ecb8e6', 'Pedro','pepe', 'ToRechulon', 'pedro@gmail.com', '{bcrypt}$2a$10$05HASeZdtwl8NS/nWbNMJOU07tiGZ9Z/mVE2Z.FKhsyjkCK7yuLqa',383838,true,true,true,true,'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQciKN1y59CDYMq-IALg7OUijN7hIiM8hdzKw&usqp=CAU');
INSERT INTO usuario_roles ( roles,usuario_id) VALUES (0,'5d818565-99f9-4d80-920e-8259c6ecb8e6');
INSERT INTO usuario (id, name,last_name, username, email, password,phone_number,ACCOUNT_NON_EXPIRED,ACCOUNT_NON_LOCKED,CREDENTIALS_NON_EXPIRED,enabled,foto_url) VALUES ('8e221cbb-7edd-4943-b643-fcb2f34deb19', 'Juan','pepe', 'ToRechulon', 'juan@gmail.com', '{bcrypt}$2a$10$05HASeZdtwl8NS/nWbNMJOU07tiGZ9Z/mVE2Z.FKhsyjkCK7yuLqa',383838,true,true,true,true,'https://previews.123rf.com/images/jemastock/jemastock1712/jemastock171209328/91942350-dise%C3%B1o-gr%C3%A1fico-del-ejemplo-del-vector-del-icono-del-avatar-del-perfil-del-hombre-de-negocios.jpg');
INSERT INTO usuario_roles ( roles,usuario_id) VALUES (0,'8e221cbb-7edd-4943-b643-fcb2f34deb19');
INSERT INTO administrador (id, name,last_name, username, email, password,phone_number,ACCOUNT_NON_EXPIRED,ACCOUNT_NON_LOCKED,CREDENTIALS_NON_EXPIRED,enabled,foto_url) VALUES ('a23c61eb-4a5a-4cdd-beea-44234583ff4c', 'Angel','perez', 'ToRechulon', 'angel@gmail.com', '{bcrypt}$2a$10$05HASeZdtwl8NS/nWbNMJOU07tiGZ9Z/mVE2Z.FKhsyjkCK7yuLqa',383838,true,true,true,true,'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQciKN1y59CDYMq-IALg7OUijN7hIiM8hdzKw&usqp=CAU');
INSERT INTO administrador_roles ( roles,administrador_id) VALUES (1,'a23c61eb-4a5a-4cdd-beea-44234583ff4c');

INSERT INTO categorias (id,nombre)values('f1ba028f-e9df-4b76-9970-c0a32a115547','panaderia');


INSERT INTO negocio (id, nombre, categorias_id, numero_empleados, telefono, email, ciudad, pais, sitioweb,usuario_id,cid) values ('d7817739-fdf0-4300-b620-00bacf17c99e', 'ElPati', 'f1ba028f-e9df-4b76-9970-c0a32a115547', 15, '574573', 'hola@gmail.com', 'sevilla', 'España', 'ElPati.com','5d818565-99f9-4d80-920e-8259c6ecb8e6','B12345678');


