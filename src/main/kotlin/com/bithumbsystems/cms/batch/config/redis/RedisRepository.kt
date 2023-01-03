package com.bithumbsystems.cms.batch.config.redis

import com.bithumbsystems.cms.batch.util.Logger
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.redisson.api.*
import org.redisson.codec.TypedJsonJacksonCodec
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisRepository(
    private val redissonClient: RedissonClient,
    private val objectMapper: ObjectMapper
) {
    private val logger by Logger()

    companion object {
        private const val WAIT_TIME = 1000L
        private const val LEASE_TIME = 3000L
    }

    /**
     * 레디스에 맵 등록 및 수정
     * @param mapKey 맵 키
     * @param valueKey 값 키
     * @param value 등록 또는 수정할 데이터
     * @param clazz 등록 또는 수정할 데이터의 클래스
     */
    fun <T> addOrUpdateRMapCacheValue(mapKey: RedisKeys, valueKey: String, value: T, clazz: Class<T>): T? =
        withLock(lockName = mapKey) {
            getRMapCache(mapKey, clazz).put(valueKey, value) ?: getRMapCacheValue(mapKey, valueKey, clazz)
        }

    /**
     * 레디스에서 맵 가져오기
     * @param mapKey 맵 키
     * @param clazz 대상 데이터의 클래스
     */
    fun <T> getRMapCache(mapKey: RedisKeys, clazz: Class<T>): RMapCache<String, T> =
        redissonClient.getMapCache(mapKey.name, TypedJsonJacksonCodec(String::class.java, clazz, objectMapper))

    fun <T> getRMapCacheValue(mapKey: RedisKeys, valueKey: String, clazz: Class<T>): T? =
        getRMapCache(mapKey, clazz).get(valueKey)

    /**
     * 레디스 맵에서 삭제
     * @param mapKey 맵 키
     * @param valueKey 값 키
     * @param clazz 대상 데이터의 클래스
     */
    fun <T> deleteRMapCacheValue(mapKey: RedisKeys, valueKey: String, clazz: Class<T>): T? = withLock(lockName = mapKey) {
        getRMapCache(mapKey, clazz).remove(valueKey)
    }

    /**
     * 레디스 셋 등록
     * @param setKey 셋 키
     * @param score 등록할 데이터의 정렬 가중치
     * @param value 등록할 데이터
     * @param clazz 등록할 데이터의 클래스
     */
    fun <T> addRScoredSortedSetValue(setKey: RedisKeys, score: Double, value: T, clazz: Class<T>): Boolean? =
        withLock(lockName = setKey) {
            getRScoredSortedSet(setKey, clazz).add(score, value)
        }

    /**
     * 레디스에서 셋 조회
     * @param setKey 셋 키
     * @param clazz 조회할 데이터의 클래스
     */
    fun <T> getRScoredSortedSet(setKey: RedisKeys, clazz: Class<T>): RScoredSortedSet<T> =
        redissonClient.getScoredSortedSet(setKey.name, TypedJsonJacksonCodec(clazz, objectMapper))

    /**
     * 레디스에서 객체 삭제
     * @param setKey 셋 키
     * @param value 삭제할 객체의 값
     * @param clazz 삭제할 데이터의 클래스
     */
    fun <T> deleteRScoredSortedSet(setKey: RedisKeys, value: T, clazz: Class<T>): Boolean? = withLock(lockName = setKey) {
        getRScoredSortedSet(setKey, clazz).remove(value)
    }

    /**
     * 레디스 셋에서 모든 값 삭제
     * @param setKey 삭제할 셋 키
     * @param clazz 대상 데이터의 클래스
     */
    fun <T> deleteAllRScoredSortedSet(setKey: RedisKeys, clazz: Class<T>): Boolean? = withLock(lockName = setKey) {
        val set: RScoredSortedSet<T> = getRScoredSortedSet(setKey, clazz)
        set.removeAll(set.readAll())
    }

    /**
     * 레디스에 리스트 등록
     * @param listKey 리스트 키
     * @param value 등록할 데이터
     * @param clazz 등록할 데이터의 클래스
     */
    fun <T> addRListValue(listKey: RedisKeys, value: T, clazz: Class<T>): Boolean? = withLock(lockName = listKey) {
        getRList(listKey, clazz).add(value)
    }

    /**
     * 레디스에 모든 리스트 등록
     * @param listKey 리스트 키
     * @param value 등록할 데이터
     * @param clazz 등록할 데이터의 클래스
     */
    fun <T> addAllRListValue(listKey: RedisKeys, value: List<T>, clazz: Class<T>): Boolean? =
        withLock(lockName = listKey) {
            getRList(listKey, clazz).addAll(value)
        }

    /**
     * 레디스에서 리스트 조회(RListReactive)
     * @param listKey 리스트 키
     * @param clazz 조회할 데이터의 클래스
     */
    fun <T> getRList(listKey: RedisKeys, clazz: Class<T>): RList<T> =
        redissonClient.getList(listKey.name, TypedJsonJacksonCodec(clazz, objectMapper))

    /**
     * 레디스에서 리스트 조회
     * @param listKey 리스트 키
     * @param clazz 조회할 데이터의 클래스
     */
    fun <T> getRListValue(listKey: RedisKeys, clazz: Class<T>): MutableList<T>? =
        getRList(listKey, clazz)

    /**
     * 레디스에서 리스트 조회 후 id로 조회
     * @param listKey 리스트 키
     * @param id 꺼낼 대상의 아이디
     * @param clazz 조회할 데이터의 클래스
     */
    fun <T> getRListValueById(listKey: RedisKeys, id: String, clazz: Class<T>): T? = getRListValue(listKey, clazz)?.find {
        it.toString().contains("id=$id")
    }

    /**
     * 레디스에서 id로 수정
     * @param listKey 리스트 키
     * @param id 수정할 대상의 아이디
     * @param updateValue 수정할 값
     * @param clazz 수정할 데이터의 클래스
     */
    fun <T> updateRListValueById(listKey: RedisKeys, id: String, updateValue: T, clazz: Class<T>) =
        withLock(lockName = listKey) {
            getIndexAndRListById(listKey, clazz, id).run {
                this.second?.let { index: Int ->
                    this.first.removeAt(index)
                    this.first.add(index, updateValue)
                }
            }
        }

    /**
     * 레디스에서 리스트 전체 삭제
     * @param listKey 리스트 키
     * @param clazz 삭제할 데이터의 클래스
     */
    fun <T> deleteRList(listKey: RedisKeys, clazz: Class<T>): Boolean? = withLock(lockName = listKey) {
        getRList(listKey, clazz).delete()
    }

    /**
     * 레디스에서 리스트 값 삭제
     * @param listKey 리스트 키
     * @param id 삭제할 대상의 아이디
     * @param clazz 삭제할 데이터의 클래스
     */
    fun <T> deleteRListValue(listKey: RedisKeys, id: String, clazz: Class<T>): T? = withLock(lockName = listKey) {
        getIndexAndRListById(listKey, clazz, id).run {
            this.second?.let { index: Int ->
                this.first.removeAt(index)
            }
        }
    }

    /**
     * 레디스에 버킷 등록 및 수정
     * @param bucketKey 버킷 키
     * @param value 등록 및 수정할 데이터
     * @param typeReference 등록 및 수정할 데이터의 TypeReference, example = object : TypeReference<List<TestData>>() {}
     */
    fun <T> addOrUpdateRBucket(bucketKey: RedisKeys, value: T, typeReference: TypeReference<T>): Unit =
        withLock(lockName = bucketKey) {
            getRBucket(bucketKey, typeReference).set(value)
        }

    /**
     * 레디스에서 버킷 조회
     * @param bucketKey 버킷 키
     * @param typeReference 조회할 데이터의 TypeReference, example = object : TypeReference<List<TestData>>() {}
     */
    fun <T> getRBucket(bucketKey: RedisKeys, typeReference: TypeReference<T>): RBucket<T> = redissonClient.getBucket(
        bucketKey.name,
        TypedJsonJacksonCodec(typeReference, objectMapper)
    )

    /**
     * 레디스에서 버킷 삭제
     * @param bucketKey 버킷 키
     * @param typeReference 삭제할 데이터의 TypeReference, example = object : TypeReference<List<TestData>>() {}
     */
    fun <T> deleteRBucket(bucketKey: RedisKeys, typeReference: TypeReference<T>): Boolean =
        withLock(lockName = bucketKey) {
            getRBucket(bucketKey, typeReference).delete()
        }

    private fun <T> getIndexAndRListById(
        listKey: RedisKeys,
        clazz: Class<T>,
        id: String
    ): Pair<RList<T>, Int?> {
        val rList: RList<T> = getRList(listKey, clazz)
        return Pair(
            rList,
            getRListValueById(listKey, id, clazz)?.let {
                rList.indexOf(it)
            }
        )
    }

    private fun <T> withLock(lockName: RedisKeys, function: () -> T): T = run {
        val functionName: String = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk {
            it.skip(2)
                .findFirst()
                .orElse(null)
        }.methodName
        logger.debug("{} method 에서 {} LOCK 취득 시도", functionName, lockName)
        val lock: RLock = redissonClient.getLock(lockName.name.plus("_LOCK"))

        if (!lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.MILLISECONDS)) {
            logger.error("{} method 에서 {} LOCK 을 획득할 수 없습니다.", functionName, lockName)
        } else {
            logger.debug("{} method 에서 {} LOCK 획득", functionName, lockName)
        }
        function().also {
            if (lock.forceUnlock()) {
                logger.debug("{} method 에서 {} LOCK 해제", functionName, lockName)
            } else {
                logger.error("{} method 에서 {} LOCK 해제 실패", functionName, lockName)
            }
        }
    }
}
