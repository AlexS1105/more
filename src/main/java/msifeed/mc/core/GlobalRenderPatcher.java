package msifeed.mc.core;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class GlobalRenderPatcher extends ClassVisitor {
    public GlobalRenderPatcher(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if ("loadRenderers".equals(name)) {
            return new LoadRenderersMethodVisitor(mv);
        }
        return mv;
    }

    private static class LoadRenderersMethodVisitor extends MethodVisitor {
        public LoadRenderersMethodVisitor(MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/client/renderer/RenderGlobal", "deleteAllDisplayLists", "()V", false);
            mv.visitIntInsn(Opcodes.BIPUSH, 32 * 2 + 2);
            mv.visitVarInsn(Opcodes.ISTORE, 1);
            mv.visitIntInsn(Opcodes.BIPUSH, 16);
            mv.visitVarInsn(Opcodes.ISTORE, 2);
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitInsn(Opcodes.IMUL);
            mv.visitVarInsn(Opcodes.ILOAD, 2);
            mv.visitInsn(Opcodes.IMUL);
            mv.visitInsn(Opcodes.ICONST_3);
            mv.visitInsn(Opcodes.IMUL);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "net/minecraft/client/renderer/GLAllocation", "generateDisplayLists", "(I)I", false);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitInsn(Opcodes.SWAP);
            mv.visitFieldInsn(Opcodes.PUTFIELD, "net/minecraft/client/renderer/RenderGlobal", "glRenderListBase", "I");
        }
    }
}

