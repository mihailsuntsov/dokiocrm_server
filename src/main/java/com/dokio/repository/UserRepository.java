/*
Приложение Dokio-server - учет продаж, управление складскими остатками, документооборот.
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU редакции 3, опубликованной Фондом свободного
программного обеспечения;
Эта программа распространяется в расчете на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу:
<http://www.gnu.org/licenses/>
 */
package com.dokio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dokio.model.User;


// Первый тип (User), переданный в дженерик JpaRepository — класс-сущность,
// с которым должен работать данный репозиторий, второй (Long) — тип первичного ключа.

//Для создания Repository нужно придерживаться несколько правил:
// 1 – Имя репозитория должно начинаться с имени сущности (ClientRepository) (необязательно).
// 2 – Второй дженерик (например Long) должен быть оберточным типом того типа которым есть
// ID нашей сущности (обязательно).
// 3 – Первый дженерик (User) должен быть объектом нашей сущности для которой мы создали репозиторий,
// это указывает на то, что Spring Data должен предоставить реализацию методов для работы
// с этой сущностью (обязательно).
// 4 – Мы должны унаследовать свой интерфейс от JpaRepository, иначе Spring Data
// не предоставит реализацию для нашего репозитория (обязательно).
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
// Имплементация не требуется. При инициализации контекста приложения Spring Data
// найдёт данный интерфейс и самостоятельно сгенерирует компонент (bean),
// реализующий данный интерфейс.

    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    User findByActivationCode(String code);
    User findByRepairPassCode(String code);
    User findByEmail(String email);
}