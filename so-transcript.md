Réda Housni Alaoui (Sep 13, 2021):
> How does this behave regarding the 2nd level cache?

Vlad Mihalcea (Sep 13, 2021):
> The 2nd level cache is thread sage and designed to be changed concurrently, so I don't see why this solution would interfere with it

Réda Housni Alaoui (Sep 13, 2021):
> Usually, the readonly database lags behind the read/write one (async replication). You could imagine fresh entries loaded in the second level cache during the readwrite transaction. After that, a read only transaction may use the fresh cached entries while they don't exist yet in the RO database, leading eventually to missing entity references on subsequent queries.

Vlad Mihalcea (Sep 14, 2021):
> If the RW Tx pushes the updated entries in the cache, then you are it means the entity will be found from the cache, not from the lagging RO DB. So, in your example, the use of the cache actually fixes the lagging problem, therefore providing an extra benefit to your system.

Réda Housni Alaoui (Sep 14, 2021):
> What you are describing works if the full entity graph is tagged for second level caching (and all entities share the same expiration policy). Most of the time, this is not the case. E.g. Entity A depends on Entity B. Entity A is marked cacheable, not B. RW creates A + B. RO transaction loads A from the cache, then tries to read B directly from the lagging RO database. B is not found by RO transaction because B is not yet in RO database.

Vlad Mihalcea (Sep 14, 2021):
> You could write a blog post demonstrating the issue and create a Hibernate Jira issue if you manage to replicate it.

Réda Housni Alaoui (Sep 14, 2021):
> This is not a Hibernate bug. This solution is buggy. The only way to make this work correctly - I can think of - is to have one EntityManager per datasource. That way, there is one second level cache instance per datasource.

Réda Housni Alaoui (Sep 14, 2021):
> Yes I used the word EntityManager instead of EntityManagerFactory, could not fix it 7 minutes later. Even a top commiter can make mistake, and sometimes recognise it, not this day it seems. People have been warned, use this art your own risk.

Vlad Mihalcea (Jul 25, 2022):
> If you cannot provide a replicating test case, it's because there is no issue. However, you do not have to use this perfectly fine solution. Feel free to find an alternative that suits you.

Réda Housni Alaoui (Oct 9, 2022):
> Here is an automated test demonstrating the issue : https://github.com/reda-alaoui/ro-rw-routing/blob/5e74d9da950b9e0a71ff91ac54023f2fce5633a9/src/test/java/me/redaalaoui/ro_rw_routing/RoRwRoutingApplicationTests.java

Vlad Mihalcea (Oct 10, 2022):
> Here's an automated test demonstrating that my solution works like a charm: https://github.com/vladmihalcea/high-performance-java-persistence/blob/master/core/src/test/java/com/vladmihalcea/book/hpjp/spring/transaction/routing/TransactionRoutingDataSourceTest.java

Réda Housni Alaoui (Oct 10, 2022):
> You don't use the 2nd level cache in your test.

Vlad Mihalcea (Oct 10, 2022):
> Yes, of course, because the 2nd-level cache is only useful for offloading the Primary node. Using it on Replica nodes is a code smell since replication already provides additional caching to your system via the extra Buffer Pools that become available for read-only transactions. The vast majority of projects who need to rely on the 2nd-level cache do it because they've used FetchTYpe.EAGER extensively, which is not really the case for me as I know how to design my data access properly. So, my solution works by design.

Réda Housni Alaoui (Oct 10, 2022):
> The subject was "is the proposed solution compatible with 2nd level cache". You have been saying "yes without a doubt" for more than a year. With your last comment, I see you are trying to steer the debate on a new subject of your own without recognizing being wrong on the first subject. But this is another way of admitting you were wrong, so thank you anyway for doing that.

Réda Housni Alaoui (Oct 10, 2022):
> Now, here is my opinion on the new subject you introduce: "2nd level cache is useless when you have a replica". If the replica is synchronous, yes. But most of the time, it is async (see subject 1). If you want better performances inside a read-write transaction, the 2nd level cache is a pretty good solution since it gives you the advantage of the async replica without the async aspect. Even with FetchType LAZY ;)

Réda Housni Alaoui (Oct 10, 2022):
> You and december 2021 you should start talking to each other. https://web.archive.org/web/20221010190902/https://vladmihalcea.com/jpa-hibernate-second-level-cache/ . « The JPA and Hibernate second-level cache is very useful when having to scale rad-write transactions. Because the second-level cache is designed to be strongly consistent, you don’t have to worry that stale data is going to be served from the cache. »

Réda Housni Alaoui (Oct 10, 2022):
> From the same source: « Scaling read-only transactions can be done fairly easily by adding more Replica nodes. However, that does not work for the Primary node since that can be only scaled vertically. And that’s where the second-level cache comes into play. For read-write database transactions that need to be executed on the Primary node, the second-level cache can help you reduce the query load by directing it to the strongly consistent second-level cache »

Réda Housni Alaoui (Oct 10, 2022):
> « Yes, of course, because the 2nd-level cache is only useful for offloading the Primary node. Using it on Replica nodes is a code smell since replication already provides additional caching to your system via the extra Buffer Pools that become available for read-only transactions. » Yes, and your solution forces us to either use it on both primary and replica, or none of them.
