package com.example.reinforce;

import org.junit.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * 方法签名
 * java类型       类型标识
 * Boolean        Z
 * byte           B
 * char           C
 * short          S
 * int            l
 * long           J
 * float          F
 * double         D
 * String        L/java/lang/String
 * int[]         [l
 * Object[]      [L/java/lang/Object
 *
 * @Author: Jack Ou
 * @CreateDate: 2020/12/10 22:17
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/12/10 22:17
 * @UpdateRemark: 更新说明
 */
public class ASMUnitTest {

    @Test
    public void test() {
        try {
            FileInputStream fis = new FileInputStream("E:\\programe\\AndroidProjectGithub\\ReinForce\\app\\src\\test\\java\\com\\example\\reinforce\\InjectTest.class");

            // 获取一个分析器，去读class文件
            ClassReader classReader = new ClassReader(fis);

            //自动计算栈针
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

            // 开始插桩
            classReader.accept(new MyClassVisitor(Opcodes.ASM7, classWriter), ClassReader.EXPAND_FRAMES);

            //执行了插桩之后的字节码数据，写入一个文件
            byte[] bytes = classWriter.toByteArray();
            FileOutputStream fos = new FileOutputStream("E:\\programe\\AndroidProjectGithub\\ReinForce\\app\\src\\test\\java\\com\\example\\reinforce\\InjectTest2.class");
            fos.write(bytes);
            fos.flush();

            fos.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 用来访问类信息
     */
    static class MyClassVisitor extends ClassVisitor {

        public MyClassVisitor(int api, ClassVisitor classVisitor) {
            super(api, classVisitor);
        }

        // 读取class文件信息的时候，每读到一个方法就执行一次这个api
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
            System.out.println(name);
            return new MyMethodVisitor(api, methodVisitor, access, name, descriptor);
        }
    }

    /**
     * 访问方法用的
     * <p>
     * 需要依赖'org.ow2.asm:asm-commons:7.1'
     */
    static class MyMethodVisitor extends AdviceAdapter {

        // 局部变量表的数值
        int start;
        int end;

        // 标记有注解的方法
        boolean inject = false;

        public MyMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor) {
            super(api, methodVisitor, access, name, descriptor);
        }

        /**
         * 当方法进入的时候执行
         */
        @Override
        protected void onMethodEnter() {
            super.onMethodEnter();
            if (!inject) return;

            //INVOKESTATIC java/lang/System.currentTimeMillis ()J
            //LSTORE 1
            // 需要方法签名   ()j  代表没有参数   返回值是long 签名是J
            invokeStatic(Type.getType("Ljava/lang/System;"), new Method("currentTimeMillis", "()J"));
            start = newLocal(Type.LONG_TYPE);
            storeLocal(start);
        }


        /**
         * 当方法退出的时候执行
         * <p>
         * GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
         * NEW java/lang/StringBuilder
         * DUP
         * INVOKESPECIAL java/lang/StringBuilder.<init> ()V
         * LDC "execute time = "
         * INVOKEVIRTUAL java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
         * LLOAD 3
         * LLOAD 1
         * LSUB
         * INVOKEVIRTUAL java/lang/StringBuilder.append (J)Ljava/lang/StringBuilder;
         * LDC "ms"
         * INVOKEVIRTUAL java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
         * INVOKEVIRTUAL java/lang/StringBuilder.toString ()Ljava/lang/String;
         * INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
         */
        @Override
        protected void onMethodExit(int opcode) {
            super.onMethodExit(opcode);
            if (!inject) return;

            invokeStatic(Type.getType("Ljava/lang/System;"), new Method("currentTimeMillis", "()J"));
            end = newLocal(Type.LONG_TYPE);
            storeLocal(end);

            getStatic(Type.getType("Ljava/lang/System;"), "out", Type.getType("Ljava/io/PrintStream;"));
            newInstance(Type.getType("Ljava/lang/StringBuilder;"));
            dup();
            invokeConstructor(Type.getType("Ljava/lang/StringBuilder;"), new Method("<init>", "()V"));
            visitLdcInsn("execute time = ");
            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), new Method("append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));

            loadLocal(end);
            loadLocal(start);
            math(SUB, Type.LONG_TYPE);
            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), new Method("append", "(J)Ljava/lang/StringBuilder;"));

            visitLdcInsn("ms");

            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), new Method("append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            invokeVirtual(Type.getType("Ljava/lang/StringBuilder;"), new Method("toString", "()Ljava/lang/String;"));
            invokeVirtual(Type.getType("Ljava/io/PrintStream;"), new Method("println", "(Ljava/lang/String;)V"));
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            System.out.println(getName() + "---->" + descriptor);
            if ("Lcom/example/reinforce/ASMTest;".equals(descriptor)){
                inject = true;
            }
            return super.visitAnnotation(descriptor, visible);
        }
    }
}
