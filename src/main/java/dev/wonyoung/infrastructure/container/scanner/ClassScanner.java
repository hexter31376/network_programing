package dev.wonyoung.infrastructure.container.scanner;

import dev.wonyoung.infrastructure.container.di.Component;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 지정한 패키지 하위를 재귀적으로 탐색하여 {@link dev.wonyoung.infrastructure.container.di.Component}
 * 어노테이션이 붙은 클래스를 수집한다.
 *
 * <h2>동작 흐름</h2>
 * <ol>
 *   <li>패키지명을 파일 경로로 변환 (예: {@code dev.wonyoung} → {@code dev/wonyoung})</li>
 *   <li>클래스로더로 해당 경로의 {@link java.io.File} 디렉터리를 얻는다</li>
 *   <li>디렉터리를 재귀 탐색하며 {@code .class} 파일을 모두 수집한다</li>
 *   <li>{@code Class.forName()}으로 클래스를 로드한 뒤 {@code @Component} 여부를 확인한다</li>
 * </ol>
 *
 * <h2>책임 범위</h2>
 * <p>이 클래스는 오직 "어떤 클래스가 컴포넌트인가"를 찾는 일만 한다.
 * 인스턴스 생성이나 의존성 주입에는 관여하지 않는다.</p>
 */
public class ClassScanner {

    private static final String CLASS_EXTENSION = ".class";

    /**
     * 지정한 패키지를 재귀 탐색하여 {@code @Component}가 붙은 클래스를 반환한다.
     *
     * @param packageName 탐색할 루트 패키지명 (예: {@code "dev.wonyoung"})
     * @return {@code @Component}가 붙은 클래스들의 집합
     * @throws IllegalArgumentException 해당 패키지 경로를 클래스로더가 찾지 못한 경우
     */
    public Set<Class<?>> scan(String packageName) throws Exception {
        List<Class<?>> allClasses = findClasses(packageName);
        Set<Class<?>> components = new HashSet<>();
        for (Class<?> clazz : allClasses) {
            if (clazz.isAnnotationPresent(Component.class)) {
                components.add(clazz);
            }
        }
        return components;
    }

    /**
     * 패키지명을 파일 경로로 변환하고 클래스로더로 루트 디렉터리를 찾은 뒤,
     * 해당 디렉터리 하위의 모든 {@code .class} 파일을 로드하여 반환한다.
     *
     * @param packageName 탐색할 패키지명
     * @return 해당 패키지 및 하위 패키지에 속한 모든 클래스 목록
     */
    private List<Class<?>> findClasses(String packageName) throws Exception {
        String path = packageName.replace('.', '/');
        URL url = getClass().getClassLoader().getResource(path);
        if (url == null) {
            throw new IllegalArgumentException("패키지를 찾을 수 없습니다: " + packageName);
        }
        List<Class<?>> classes = new ArrayList<>();
        collectClasses(new File(url.toURI()), packageName, classes);
        return classes;
    }

    /**
     * 디렉터리를 재귀적으로 탐색하여 {@code .class} 파일을 {@code classes}에 누적한다.
     *
     * <p>하위 디렉터리를 만나면 패키지명을 {@code 패키지명.디렉터리명} 형태로 이어 붙여 재귀 호출한다.
     * {@code .class} 파일을 만나면 확장자를 제거한 전체 클래스명으로 {@code Class.forName()}을 호출한다.</p>
     *
     * @param dir         현재 탐색 중인 디렉터리
     * @param packageName 현재 디렉터리에 해당하는 패키지명
     * @param classes     발견한 클래스를 누적할 리스트
     */
    private void collectClasses(File dir, String packageName, List<Class<?>> classes) throws Exception {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                collectClasses(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(CLASS_EXTENSION)) {
                String className = packageName + "." + file.getName().replace(CLASS_EXTENSION, "");
                classes.add(Class.forName(className));
            }
        }
    }
}