/*
Copyright © 2020 Сунцов Михаил Александрович. mihail.suntsov@yandex.ru
Эта программа является свободным программным обеспечением: Вы можете распространять ее и (или) изменять,
соблюдая условия Генеральной публичной лицензии GNU Affero GPL редакции 3 (GNU AGPLv3),
опубликованной Фондом свободного программного обеспечения;
Эта программа распространяется в расчёте на то, что она окажется полезной, но
БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ, включая подразумеваемую гарантию КАЧЕСТВА либо
ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Ознакомьтесь с Генеральной публичной
лицензией GNU для получения более подробной информации.
Вы должны были получить копию Генеральной публичной лицензии GNU вместе с этой
программой. Если Вы ее не получили, то перейдите по адресу: http://www.gnu.org/licenses
*/
package com.dokio.repository.Exceptions;

// Используем при отмене проведения исходящих платежей (Исходящий платеж, Расходный ордер, Выемка), если входящий платеж уже проведён
// В этом случае сначала нужно отменить проведение входящего платежа (Входящий плтаёж, Приходный ордер, Внесение), а затем уже отменять
// проведение исходящего
public class IncomingPaymentIsCompletedException extends Exception {
    @Override
    public void printStackTrace() {
        System.err.println("Can't do operation because outcoming payment is not completed");
    }
}
